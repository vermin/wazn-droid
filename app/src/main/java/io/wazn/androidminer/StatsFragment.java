// Copyright (c) 2019, Mine2Gether.com
//
// Please see the included LICENSE file for more information.
//
// Copyright (c) 2020-2021 Project Wazn
// Copyright (c) 2021 Scala
//
// Please see the included LICENSE file for more information.

package io.wazn.androidminer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

import io.wazn.androidminer.api.ProviderData;
import io.wazn.androidminer.api.PoolItem;
import io.wazn.androidminer.api.IProviderListener;
import io.wazn.androidminer.api.ProviderManager;

public class StatsFragment extends Fragment {
    protected static IProviderListener statsListener;

    public static ProviderData poolData = null;

    static final View view = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        statsListener = new IProviderListener() {
            public void onStatsChange(ProviderData d) {
                updateFields(d, view);
            }

            @Override
            public boolean onEnabledRequest() {
                return true;
            }
        };

        LinearLayout llPayments = view.findViewById(R.id.llPayments);
        llPayments.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onShowPayments();
            }
        });

        ProviderManager.request.setListener(statsListener).start();
        ProviderManager.afterSave();

        updateFields(ProviderManager.data, view);

        checkValidState();

        PoolItem pi = ProviderManager.getSelectedPool();

        ImageView ivShowPayments = view.findViewById(R.id.ivShowPayments);
        ivShowPayments.setVisibility(pi.getPoolType() == 0 ? View.GONE : View.VISIBLE);

        return view;
    }

    public static void updateStatsListener() {
        ProviderManager.fetchStats();
        updateFields(ProviderManager.data, view);
    }

    private static void updateFields(ProviderData d, View view) {
        poolData = d;

        if(view == null || view.getContext() == null)
            return;

        if(d.isNew) {
            return;
        }

        PoolItem pi = ProviderManager.getSelectedPool();

        ImageView ivShowPayments = view.findViewById(R.id.ivShowPayments);
        ivShowPayments.setVisibility(pi.getPoolType() == 0 ? View.GONE : View.VISIBLE);

        // Network

        String[] n = d.network.hashrate.split(" ");
        TextView tvNetworkHashrate = view.findViewById(R.id.hashratenetwork);
        tvNetworkHashrate.setText(n.length > 0 ? n[0] : "n/a");

        TextView tvNetworkHashrateUnit = view.findViewById(R.id.hashratenetwork_unit);
        tvNetworkHashrateUnit.setText(n.length > 1 ? n[1] : "MH/s");

        TextView tvNetworkDifficulty = view.findViewById(R.id.difficultynetwork);
        tvNetworkDifficulty.setText(d.network.difficulty.isEmpty() ? "n/a" : d.network.difficulty);

        TextView tvNetworkBlocks = view.findViewById(R.id.lastblocknetwork);
        tvNetworkBlocks.setText(d.network.lastBlockTime.isEmpty() ? "n/a" : d.network.lastBlockTime);

        TextView tvNetworkHeight = view.findViewById(R.id.height);
        tvNetworkHeight.setText(d.network.lastBlockHeight.isEmpty() ? "n/a" : String.format(Locale.getDefault(), "%,d", Long.parseLong(d.network.lastBlockHeight)));

        TextView tvNetworkRewards = view.findViewById(R.id.rewards);
        tvNetworkRewards.setText(d.network.lastRewardAmount.isEmpty() ? "n/a" : d.network.lastRewardAmount);

        // Pool

        TextView tvPoolURL = view.findViewById(R.id.poolurl);
        tvPoolURL.setText(pi.getPool() == null ? "" : pi.getPool());

        String[] p = d.pool.hashrate.split(" ");
        TextView tvPoolHashrate = view.findViewById(R.id.hashratepool);
        tvPoolHashrate.setText(p.length > 0 ? p[0] : "n/a");

        TextView tvPoolHashrateUnit = view.findViewById(R.id.hashratepool_unit);
        tvPoolHashrateUnit.setText(p.length > 1 ? p[1] : "kH/s");

        TextView tvPoolMiners = view.findViewById(R.id.miners);
        tvPoolMiners.setText(d.pool.miners.isEmpty() ? "n/a" : String.format(Locale.getDefault(), "%,d", Integer.parseInt(d.pool.miners)));

        TextView tvPoolLastBlock = view.findViewById(R.id.lastblockpool);
        tvPoolLastBlock.setText(d.pool.lastBlockTime.isEmpty() ? "n/a" : d.pool.lastBlockTime);

        // Pool Blocks
        LinearLayout llPoolBlocks = view.findViewById(R.id.llBlocksPool);
        TextView tvPoolLBlocks = view.findViewById(R.id.blockspool);

        // Not available for cryptonote-nodejs-pool and custom pools
        if(pi.getPoolType() == 2 || pi.getPoolType() == 0) {
            tvPoolLBlocks.setText("n/a");
            llPoolBlocks.setVisibility(View.GONE);
        } else {
            tvPoolLBlocks.setText(d.pool.blocks.isEmpty() ? "n/a" : String.format(Locale.getDefault(), "%,d", Long.parseLong(d.pool.blocks)));
            llPoolBlocks.setVisibility(View.VISIBLE);
        }

        // Address

        String wallet = Config.read(Config.CONFIG_ADDRESS);
        String prettyaddress = "";
        if(!wallet.isEmpty())
            prettyaddress = wallet.substring(0, 7) + "..." + wallet.substring(wallet.length() - 7);

        TextView tvWalletAddress = view.findViewById(R.id.walletaddress);
        tvWalletAddress.setText(prettyaddress);

        String sHashrate = d.miner.hashrate;
        TextView tvAddressHashrate = view.findViewById(R.id.hashrateminer);
        TextView tvAddressHashrateUnit = view.findViewById(R.id.hashrateminer_unit);

        if(!sHashrate.isEmpty()) {
            String[] a = sHashrate.split(" ");

            tvAddressHashrate.setText(a.length > 0 ? a[0] : "n/a");
            tvAddressHashrateUnit.setText(a.length > 1 ? a[1] : "H/s");
        } else {
            tvAddressHashrate.setText(pi.getPoolType() == 0 ? "n/a" : "0");
            tvAddressHashrateUnit.setText("H/s");
        }

        tvAddressHashrate.setTextColor(tvAddressHashrate.getText().equals("0") || tvAddressHashrate.getText().equals("n/a") ? view.getResources().getColor(R.color.txt_main) : view.getResources().getColor(R.color.c_green));

        TextView tvAddressLastShare = view.findViewById(R.id.lastshareminer);
        tvAddressLastShare.setText(d.miner.lastShare.isEmpty() ? "n/a" : d.miner.lastShare);

        TextView tvAddressSubmittedHash = view.findViewById(R.id.submittedhash);
        tvAddressSubmittedHash.setText(pi.getPoolType() == 1 || pi.getPoolType() == 2 ? view.getResources().getString(R.string.submitted_shares) : view.getResources().getString(R.string.submitted_hashes));

        TextView tvAddressBlocks = view.findViewById(R.id.blocksminedminer);
        tvAddressBlocks.setText(d.miner.shares.isEmpty() ? "n/a" : String.format(Locale.getDefault(), "%,d", Long.parseLong(d.miner.shares)));

        String sBalance = d.miner.balance.replace("WAZN", "").trim();
        TextView tvBalance = view.findViewById(R.id.balance);
        tvBalance.setText(d.miner.balance.isEmpty() ? pi.getPoolType() == 0 ? "n/a" : Tools.getLongValueString(0.0) : sBalance);

        String sPaid = d.miner.paid.replace("WAZN", "").trim();
        TextView tvPaid = view.findViewById(R.id.paid);
        tvPaid.setText(d.miner.paid.isEmpty() ? pi.getPoolType() == 0 ? "n/a" : Tools.getLongValueString(0.0) : sPaid);
        tvPaid.setTextSize(sPaid.length() > 6 ? 12 : 14);

        TextView tvPaidUnit = view.findViewById(R.id.paid_unit);
        tvPaidUnit.setTextSize(sPaid.length() > 6 ? 12 : 14);
    }

    public void onShowPayments() {
        PoolItem pi = ProviderManager.getSelectedPool();

        if(pi.getPoolType() != 0)
            startActivity(new Intent(getActivity(), PaymentsActivity.class));
    }

    public void checkValidState() {
        if(getContext() == null)
            return;

        if(Config.read(Config.CONFIG_ADDRESS).equals("")) {
            Utils.showToast(getContext(),"Wallet address is empty.", Toast.LENGTH_LONG);
            return;
        }

        PoolItem pi = ProviderManager.getSelectedPool();

        if (!Config.read(Config.CONFIG_INIT).equals("1") || pi == null) {
            Utils.showToast(getContext(),"Start mining to view statistics.", Toast.LENGTH_LONG);
            return;
        }

        if (pi.getPoolType() == 0) {
            Utils.showToast(getContext(),"Statistics are not available for custom pools.", Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ProviderManager.request.setListener(statsListener).start();
    }
}
