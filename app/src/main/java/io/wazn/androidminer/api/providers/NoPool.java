package io.wazn.androidminer.api.providers;

import io.wazn.androidminer.api.ProviderAbstract;
import io.wazn.androidminer.api.PoolItem;

public final class NoPool extends ProviderAbstract {

    public NoPool(PoolItem poolItem){
        super(poolItem);
    }

    @Override
    protected void onBackgroundFetchData() {}
}
