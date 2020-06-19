package com.bokecc.vod.view;

import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;

public class EmoFilter implements InputFilter {

    @Override
    public CharSequence filter(CharSequence charSequence, int start, int end, Spanned spanned, int i2, int i3) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = start; i < end; i++) {
            char charContent = charSequence.charAt(i);
            if (!isEmo(charContent)) {
                stringBuffer.append(charContent);
            } else {
                i++;
            }
        }
        if (charSequence instanceof Spanned) {
            SpannableString sp = new SpannableString(stringBuffer);
            TextUtils.copySpansFrom((Spanned) charSequence, start, end, null,
                    sp, 0);
            return sp;
        } else {
            return stringBuffer;
        }
    }

    public boolean isEmo(char charContent) {
        if ((charContent == 0x0) || (charContent == 0x9) || (charContent == 0xA)
                || (charContent == 0xD)
                || ((charContent >= 0x20) && (charContent <= 0x29))
                || ((charContent >= 0x2A) && (charContent <= 0x3A))
                || ((charContent >= 0x40) && (charContent <= 0xA8))
                || ((charContent >= 0xAF) && (charContent <= 0x203B))
                || ((charContent >= 0x203D) && (charContent <= 0x2048))
                || ((charContent >= 0x2050) && (charContent <= 0x20e2))
                || ((charContent >= 0x20e4) && (charContent <= 0x2100))
                || ((charContent >= 0x21AF) && (charContent <= 0x2300))
                || ((charContent >= 0x23FF) && (charContent <= 0X24C1))
                || ((charContent >= 0X24C3) && (charContent <= 0x2500))
                || ((charContent >= 0x2800) && (charContent <= 0x2933))
                || ((charContent >= 0x2936) && (charContent <= 0x2AFF))
                || ((charContent >= 0x2C00) && (charContent <= 0x3029))
                || ((charContent >= 0x3031) && (charContent <= 0x303C))
                || ((charContent >= 0x303E) && (charContent <= 0x3296))
                || ((charContent >= 0x32A0) && (charContent <= 0xD7FF))
                || ((charContent >= 0xE000) && (charContent <= 0xFE0E))
                || ((charContent >= 0xFE10) && (charContent <= 0xFFFD))
                || ((charContent >= 0x10000) && (charContent <= 0x10FFFF))) {
            return false;
        }
        return true;
    }
}
