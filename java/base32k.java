package com.encoding.base32k;

public class Base32k {

    // Convert a Unicode code point to a 15-bit int
    static private int u2i(char u) {
	if (u >= 0x3400 && u <= 0x4DB5) {
	    return u - 0x3400;
	} else if (u >= 0x4E00 && u <= 0x9FA5) {
	    return u - (0x4E00 - 6582);
	} else if (u >= 0xE000 && u <= 0xF4A3) {
	    return u - (0xE000 - 27484);
	} else {
	    return -1;// throw "Invalid encoding U+" + ("000" +
		     // u.toString(16).toUpperCase()).slice(-4);
	}
    }

    // Convert a 15-bit int to a Unicode code point
    static private char i2u(int i) {
	int out;
	if (i < 6582) {
	    out = 0x3400 + i;
	} else if (i < 27484) {
	    out = 0x4E00 + i - 6582;
	} else {
	    out = 0xE000 + i - 27484;
	}
	return (char) out;
    };

    static String encodeBytes(byte[] a) {
	String out = "";
	int v = 0, o = 0;
	int i = 0;
	int u = 0;
	while (i < a.length) {
	    v = (v << 8) | (a[i]+128);
	    u += 8;
	    while (u >= 15) {
		o = v >> (u % 15);
		out += i2u(o & 0x7FFF);
		u -= 15;
	    }
	    i += 1;
	}
	if(u!=0) {
	    o = v << (15-u);
	    out += i2u(o & 0x7FFF);
	} else {
	    u = 15;
	}
	out += (char) (0x2400 + u); // terminator
	return out;
    };

// quick and dirty debug  with GWT
//    static native void debug(String s) /*-{
//		console.log(s);
//    }-*/;

    static byte[] decodeBytes(String s) {
	int tailbits = s.charAt(s.length() - 1) - 0x2400;
	if (tailbits < 1 || tailbits > 15) {
	    //debug("Invalid encoding");
	    return null;
	}
	int len = ((s.length() - 2) * 15 + tailbits) / 8;
	byte[] out = new byte[len];
	int p = 0;
	int q, r, i, m = 0;
	int bs = 0;
	int cnt = 0;
	int v;
	for (i = 0; i < s.length() - 1; i++) {
	    v = u2i(s.charAt(i));
	    if(v==-1) {
		//debug("Invalid encoding");
		return null;
	    }
	    p = (p << 15) | v;
	    bs += 15;
	    while ((bs >= 8) && (cnt < len)) {
		q = bs / 8 - 1;
		r = bs % 8;
		m = ((p >> (q * 8 + r)) & 0xFF) - 128;
		out[cnt] = (byte) m;
		cnt += 1;
		bs -= 8;
	    }
	}
	return out;
    };
}
