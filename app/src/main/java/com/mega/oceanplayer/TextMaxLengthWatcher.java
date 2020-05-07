package com.mega.oceanplayer;

import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.widget.EditText;

import java.io.UnsupportedEncodingException;

public class TextMaxLengthWatcher implements TextWatcher {

    private int maxLen = 0;
    private EditText editText = null;

    public TextMaxLengthWatcher(EditText editText, int maxLen) {
        this.maxLen = maxLen;
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

        Editable editable = editText.getText();
        String str = editable.toString() + "";

        int len = s.length();

        if (len > maxLen) {
            editText.removeTextChangedListener(this);
            int selEndIndex = Selection.getSelectionEnd(editable);

            String newStr = str.substring(0, maxLen);
            editText.setText(newStr);

            editable = editText.getText();

            //新字符串的长度
            int newLen = editable.length();

            //旧光标位置超过字符串长度
            if (selEndIndex > newLen) {
                selEndIndex = editable.length();
            }
            //设置新光标所在的位置
            Selection.setSelection(editable, selEndIndex);

            editText.addTextChangedListener(this);
        }
    }
}
