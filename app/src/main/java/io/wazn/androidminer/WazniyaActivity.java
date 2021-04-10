// Copyright (c) 2020-2021 Project Wazn
// Copyright (c) 2021 Scala
//
// Please see the included LICENSE file for more information.

package io.wazn.androidminer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class WazniyaActivity extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            // Activity was brought to front and not created,
            // Thus finishing this will get us to the last viewed activity
            finish();
            return;
        }

        setContentView(R.layout.fragment_wazniya);
    }

    public void onCloseWazniya(View view) {
        super.onBackPressed();
    }

    public void onPlayStore(View view) {
        Uri uri = Uri.parse(getResources().getString(R.string.wazn_wazniya_play_store));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
