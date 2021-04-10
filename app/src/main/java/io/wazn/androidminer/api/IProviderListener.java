// Copyright (c) 2020-2021 Project Wazn
// Copyright (c) 2021 Scala
//
// Please see the included LICENSE file for more information.

package io.wazn.androidminer.api;

public interface IProviderListener {
    void onStatsChange(ProviderData data);

    boolean onEnabledRequest();
}
