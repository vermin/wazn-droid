// Copyright (c) 2020-2021 Wazn Project
// Copyright (c) 2020-2021 Project Wazn
// Copyright (c) 2021 Scala
// Copyright (c) 2019, Mine2Gether.com
//
// Please see the included LICENSE file for more information.

package io.wazn.androidminer.api.providers;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;

import io.wazn.androidminer.Utils;
import io.wazn.androidminer.PoolActivity;
import io.wazn.androidminer.api.ProviderData;
import io.wazn.androidminer.network.Json;
import io.wazn.androidminer.api.ProviderAbstract;
import io.wazn.androidminer.api.PoolItem;
import io.wazn.androidminer.widgets.PoolInfoAdapter;

import static io.wazn.androidminer.Tools.getReadableDifficultyString;
import static io.wazn.androidminer.Tools.getReadableHashRateString;
import static io.wazn.androidminer.Tools.parseCurrency;
import static io.wazn.androidminer.Tools.tryParseLong;

public class CryptonoteNodejsPool extends ProviderAbstract {

    public CryptonoteNodejsPool(PoolItem pi){
        super(pi);
    }

    public StringRequest getStringRequest(PoolInfoAdapter poolsAdapter) {
        String url = mPoolItem.getApiUrl().isEmpty() ?  mPoolItem.getPool() : mPoolItem.getApiUrl();
        url += "/stats";

        return new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        JSONObject objConfigPool = obj.getJSONObject("pool");

                        // Miners
                        mPoolItem.setMiners(objConfigPool.getInt("miners"));

                        // Hashrate
                        mPoolItem.setHr(Utils.convertStringToFloat(objConfigPool.getString("hashrate")) / 1000.0f);

                        mPoolItem.setIsValid(true);
                    } catch (Exception e) {
                        mPoolItem.setIsValid(false);
                    }
                    finally {
                        poolsAdapter.dataSetChanged();
                    }
                }
                , PoolActivity::parseVolleyError);
    }

    @Override
    protected void onBackgroundFetchData() {
        PrettyTime pTime = new PrettyTime();
        ProviderData mBlockData = getBlockData();
        mBlockData.isNew = false;

        try {
            String url = mPoolItem.getApiUrl() + "/stats";
            String dataStatsNetwork  = Json.fetch(url);
            Log.i(LOG_TAG, dataStatsNetwork);

            JSONObject joStats = new JSONObject(dataStatsNetwork);
            JSONObject joStatsConfig = joStats.getJSONObject("config");
            JSONObject joStatsLastBlock = joStats.has("lastblock") ? joStats.getJSONObject("lastblock") : null;
            JSONObject joStatsNetwork = joStats.getJSONObject("network");
            JSONObject joStatsPool = joStats.getJSONObject("pool");

            mBlockData.coin.name = joStatsConfig.optString("coin").toUpperCase();
            mBlockData.coin.units = tryParseLong(joStatsConfig.optString("coinUnits"), 1L);
            mBlockData.coin.symbol = joStatsConfig.optString("symbol").toUpperCase();
            mBlockData.coin.denominationUnit = tryParseLong(joStatsConfig.optString("denominationUnit"), 1L);

            //mBlockData.pool.lastBlockHeight = joStatsPool.optString("height");
            mBlockData.pool.lastBlockTime = pTime.format(new Date(joStatsPool.optLong("lastBlockFound")));
            //mBlockData.pool.lastRewardAmount = parseCurrency(joStatsLastBlock.optString("reward", "0"), mBlockData.coin.units, mBlockData.coin.denominationUnit, mBlockData.coin.symbol);
            mBlockData.pool.hashrate = getReadableHashRateString(tryParseLong(joStatsPool.optString("hashrate"),0L));
            mBlockData.pool.blocks = joStatsPool.optString("roundHashes", "0");
            mBlockData.pool.miners = joStatsPool.optString("miners", "0");
            mBlockData.pool.minPayout = parseCurrency(joStatsConfig.optString("minPaymentThreshold", "0"), mBlockData.coin.units, mBlockData.coin.denominationUnit, mBlockData.coin.symbol);

            mBlockData.network.lastBlockHeight = joStatsNetwork.optString("height");
            mBlockData.network.difficulty = getReadableDifficultyString(joStatsNetwork.optLong("difficulty"));

            long lastBlock = (joStatsLastBlock != null ? joStatsLastBlock.optLong("timestamp") : joStatsNetwork.optLong("timestamp")) * 1000;
            mBlockData.network.lastBlockTime = pTime.format(new Date(lastBlock));

            String lastReward = joStatsLastBlock != null ? joStatsLastBlock.optString("reward", "0") : joStatsNetwork.optString("reward", "0");
            mBlockData.network.lastRewardAmount = parseCurrency(lastReward, mBlockData.coin.units, mBlockData.coin.denominationUnit, mBlockData.coin.symbol);

            mBlockData.network.hashrate = getReadableHashRateString(joStatsNetwork.optLong("difficulty") / 120L);
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
            String url = mPoolItem.getApiUrl() + "/stats_address?address=" + getWalletAddress();

            String dataWallet  = Json.fetch(url);

            JSONObject joStatsAddress = new JSONObject(dataWallet);
            JSONObject joStatsAddressStats = joStatsAddress.getJSONObject("stats");

            ProviderData.Coin coin = mBlockData.coin;
            String hashRate = joStatsAddressStats.optString("hashrate", "0");
            String balance = parseCurrency(joStatsAddressStats.optString("balance", "0"), coin.units, coin.denominationUnit, coin.symbol);
            String paid = parseCurrency(joStatsAddressStats.optString("paid", "0"), coin.units, coin.denominationUnit, coin.symbol);
            String lastShare = pTime.format(new Date(joStatsAddressStats.optLong("lastShare") * 1000));
            String blocks = String.valueOf(tryParseLong(joStatsAddressStats.optString("shares_good"), 0L));

            Log.i(LOG_TAG, "hashRate: " + hashRate);

            mBlockData.miner.hashrate = hashRate;
            mBlockData.miner.balance = balance;
            mBlockData.miner.paid = paid;
            mBlockData.miner.lastShare = lastShare;
            mBlockData.miner.shares = blocks;

            // Payments
            mBlockData.miner.payments.clear();

            //TODO

        } catch (JSONException e) {
            Log.i(LOG_TAG, "ADDRESS\n" + e.toString());
            e.printStackTrace();
        }
    }
}
