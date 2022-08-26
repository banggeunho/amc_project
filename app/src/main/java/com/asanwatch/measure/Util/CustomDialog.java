package com.asanwatch.measure.Util;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.asanwatch.measure.R;
import com.asanwatch.measure.databinding.ActivityMainBinding;

/**
 * Created by Administrator on 2017-08-07.
 */

public class CustomDialog {

    private Context context;

    public CustomDialog(Context context) {
        this.context = context;
    }

    // 호출할 다이얼로그 함수를 정의한다.
    public void showDialog(String msg) {

        // 커스텀 다이얼로그를 정의하기위해 Dialog클래스를 생성한다.
        final Dialog dlg = new Dialog(context);

        // 커스텀 다이얼로그의 레이아웃을 설정한다.
        dlg.setContentView(R.layout.dialog_confirm);

        // 커스텀 다이얼로그를 노출한다.
        // 커스텀 다이얼로그의 각 위젯들을 정의한다.
        final TextView message = (TextView) dlg.findViewById(R.id.message);
        final Button okButton = (Button) dlg.findViewById(R.id.yesButton);
        final Button cancelButton = (Button) dlg.findViewById(R.id.noButton);

        message.setText(msg);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // '확인' 버튼 클릭시 메인 액티비티에서 설정한 main_label에
                // 커스텀 다이얼로그에서 입력한 메시지를 대입한다.
                Toast.makeText(context, "\"" +  message.getText().toString() + "\" 을 입력하였습니다.", Toast.LENGTH_SHORT).show();

                // 커스텀 다이얼로그를 종료한다.
                dlg.dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "취소 했습니다.", Toast.LENGTH_SHORT).show();

                // 커스텀 다이얼로그를 종료한다.
                dlg.dismiss();
            }
        });

        dlg.show();
    }
}
