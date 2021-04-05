package io.wazn.androidminer.api.providers;

import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;

import io.wazn.androidminer.R;
import io.wazn.androidminer.api.ProviderData;
import io.wazn.androidminer.network.Json;
import io.wazn.androidminer.api.ProviderAbstract;
import io.wazn.androidminer.api.PoolItem;

import static io.wazn.androidminer.Tools.getReadableHashRateString;
import static io.wazn.androidminer.Tools.parseCurrency;
import static io.wazn.androidminer.Tools.tryParseLong;

public class IoTProxy extends ProviderAbstract {

    public IoTProxy(PoolItem pi){
        super(pi);
    }

    @Override
    protected void onBackgroundFetchData() {
        PrettyTime pTime = new PrettyTime();
        ProviderData mBlockData = getBlockData();
        mBlockData.isNew = false;

        try {
            String url = mPoolItem.getApiUrl() + "stats&wsd";
            String dataStatsNetwork  = Json.fetch(url);
            Log.i(LOG_TAG, dataStatsNetwork);

            JSONObject joStats = new JSONObject(dataStatsNetwork);
            //JSONObject joStatsConfig = joStats.getJSONObject("config");
            //JSONObject joStatsLastBlock = joStats.getJSONObject("lastblock");
            JSONObject joStatsNetwork = joStats.getJSONObject("network");
            JSONObject joStatsPool = joStats.getJSONObject("pool");
            JSONObject joStatsPool2 = joStats.getJSONObject("pool_statistics");
            //JSONObject joStatsPoolStats = joStatsPool.getJSONObject("stats");

            //mBlockData.coin.name = joStatsConfig.optString("coin").toUpperCase();
            //mBlockData.coin.units = tryParseLong(joStatsConfig.optString("coinUnits"), 1L);
            //mBlockData.coin.symbol = joStatsConfig.optString("symbol").toUpperCase();
            //mBlockData.coin.denominationUnit = tryParseLong(joStatsConfig.optString("denominationUnit"), 1L);
            mBlockData.pool.miners = joStatsPool2.optString("miners", "0");
            mBlockData.pool.difficulty = getReadableHashRateString(joStatsPool.optLong("totalDiff"));
            mBlockData.pool.lastBlockTime = pTime.format(new Date(joStatsPool.optLong("lastblock_timestamp") * 1000));
            mBlockData.pool.lastRewardAmount = parseCurrency(joStatsPool.optString("lastblock_lastReward", "0"), mBlockData.coin.units, mBlockData.coin.denominationUnit, mBlockData.coin.symbol);
            mBlockData.pool.hashrate = joStatsPool.optString("hashrate", "0");
            //String.valueOf(tryParseLong(joStatsPool.optString("hashrate"),0L)); /// 1000L
            mBlockData.pool.blocks = joStatsPool.optString("blocksFound", "0L");
            mBlockData.pool.minPayout = parseCurrency(joStatsPool.optString("minPaymentThreshold", "0"), mBlockData.coin.units, mBlockData.coin.denominationUnit, mBlockData.coin.symbol);

            mBlockData.network.lastBlockHeight = joStatsNetwork.optString("height");
            mBlockData.network.difficulty = getReadableHashRateString(joStatsNetwork.optLong("difficulty"));
            mBlockData.network.lastBlockTime = pTime.format(new Date(joStatsNetwork.optLong("timestamp") * 1000));
            mBlockData.network.lastRewardAmount = joStatsNetwork.optString("lastblock_lastReward", "0");
        } catch (JSONException e) {
            Log.i(LOG_TAG, "NETWORK\n" + e.toString());
            e.printStackTrace();
        }

        String wallet = getWalletAddress();
        Log.i(LOG_TAG, "Wallet: " + wallet);
        if(wallet.equals("")) {
            return;
        }
        try {
            String url = mPoolItem.getApiUrl() + "stats&wallet=" + getWalletAddress();

            String dataWallet  = Json.fetch(url);

            JSONObject joStatsAddress = new JSONObject(dataWallet);
            JSONObject joStatsAddressStats = joStatsAddress.getJSONObject("stats");

            ProviderData.Coin coin = mBlockData.coin;
            String hashRate = joStatsAddressStats.optString("hashrate", "0");
            String balance = joStatsAddressStats.optString("balance", "0");
            String paid = joStatsAddressStats.optString("totalPaid", "0");
            String lastShare = pTime.format(new Date(joStatsAddressStats.optLong("lastChange") * 1000));
            String blocks = String.valueOf(tryParseLong(joStatsAddressStats.optString("totalHashes"), 0L));

            Log.i(LOG_TAG, "hashRate: " + hashRate);

            mBlockData.miner.hashrate = hashRate;
            mBlockData.miner.balance = balance;
            mBlockData.miner.paid = paid;
            mBlockData.miner.lastShare = lastShare;
            mBlockData.miner.blocks = blocks;
        } catch (JSONException e) {
            Log.i(LOG_TAG, "ADDRESS\n" + e.toString());
            e.printStackTrace();
        }
    }
}
