// Copyright (c) 2020-2021 Project Wazn
// Copyright (c) 2021 Scala
//
// Please see the included LICENSE file for more information.

package io.wazn.androidminer.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.wazn.androidminer.R;
import io.wazn.androidminer.Utils;
import io.wazn.androidminer.api.PoolItem;
import io.wazn.androidminer.api.ProviderManager;

public class PoolInfoAdapter extends RecyclerView.Adapter<PoolInfoAdapter.ViewHolder> {

    public interface OnMenuPoolListener {
        boolean onContextInteraction(MenuItem item, PoolItem infoItem);
    }

    public interface OnSelectPoolListener {
        void onSelectPool(PoolItem item);
    }

    private final List<PoolItem> poolItems = new ArrayList<>();

    private final OnMenuPoolListener onMenuPoolListener;
    private final OnSelectPoolListener onSelectPoolListener;

    private final Context context;

    public PoolInfoAdapter(Context context, OnSelectPoolListener onSelectPoolListener, OnMenuPoolListener onMenuPoolListener) {
        this.context = context;
        this.onSelectPoolListener = onSelectPoolListener;
        this.onMenuPoolListener = onMenuPoolListener;
    }

    @Override
    public @NonNull
    ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pool, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final @NonNull ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return poolItems.size();
    }

    public void addPool(PoolItem pool) {
        if (!poolItems.contains(pool))
            poolItems.add(pool);

        dataSetChanged(); // in case the poolitem has changed
    }

    public void deletePool(PoolItem pool) {
        poolItems.remove(pool);

        dataSetChanged(); // in case the poolitem has changed
    }

    public void dataSetChanged() {
        notifyDataSetChanged();
    }

    public void setPools(Collection<PoolItem> data) {
        poolItems.clear();
        if (data != null) {
            poolItems.addAll(data);
        }

        Collections.sort(poolItems, PoolItem.PoolComparator);

        dataSetChanged();
    }

    private boolean itemsClickable = true;

    public void allowClick(boolean clickable) {
        itemsClickable = clickable;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView tvName;
        final TextView tvMiners;
        final TextView tvHr;
        final ImageView ivIcon;
        final ImageButton ibOptions;
        boolean popupOpen = false;

        PoolItem poolItem;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvMiners = itemView.findViewById(R.id.tvMiners);
            tvHr = itemView.findViewById(R.id.tvHr);
            ivIcon = itemView.findViewById(R.id.ivIcon);

            ibOptions = itemView.findViewById(R.id.ibOptions);
            ibOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (popupOpen)
                        return;

                    PopupMenu popup = new PopupMenu(context, ibOptions);
                    popup.inflate(R.menu.pool_context_menu);
                    popupOpen = true;

                    MenuItem itemEdit = popup.getMenu().findItem(R.id.action_edit_pool);
                    itemEdit.setTitle(context.getResources().getString(R.string.edit));

                    MenuItem itemDelete = popup.getMenu().findItem(R.id.action_delete_pool);
                    itemDelete.setVisible(poolItem.getPoolType() == 0);

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (onMenuPoolListener != null) {
                                return onMenuPoolListener.onContextInteraction(item, poolItem);
                            }
                            return false;
                        }
                    });

                    popup.show();
                    popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
                        @Override
                        public void onDismiss(PopupMenu menu) {
                            popupOpen = false;
                        }
                    });
                }
            });
        }

        void bind(final int position) {
            poolItem = poolItems.get(position);

            tvName.setText(poolItem.getKey());

            // Stats
            if(poolItem.isValid()) {
                // Miners
                tvMiners.setVisibility(View.VISIBLE);
                tvMiners.setText(String.format("%s %s", poolItem.getMiners(), context.getResources().getString(R.string.miners)));

                // Hashrate
                tvHr.setVisibility(View.VISIBLE);
                float hashrate = poolItem.getHr();

                String frmt = "k";
                if(hashrate > 1000) {
                    frmt = "M";
                    hashrate /= 1000.0f;
                }

                tvHr.setText(String.format("%s %sH/s", new DecimalFormat("##.#").format(hashrate), frmt));
            } else {
                tvMiners.setVisibility(View.GONE);
                tvHr.setVisibility(View.GONE);
            }

            // Icon
            Bitmap icon = poolItem.getIcon();
            if(icon != null) {
                int dim = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 34, itemView.getResources().getDisplayMetrics());
                ivIcon.getLayoutParams().height = dim;
                ivIcon.getLayoutParams().width = dim;

                ivIcon.setImageBitmap(Utils.getCroppedBitmap(poolItem.getIcon()));
            }
            else {
                int dim = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, itemView.getResources().getDisplayMetrics());
                ivIcon.getLayoutParams().height = dim;
                ivIcon.getLayoutParams().width = dim;

                ivIcon.setImageBitmap(ProviderManager.getDefaultPoolIcon(context, poolItem));
            }

            // Selected view layout
            RelativeLayout rlItemNode = (RelativeLayout) itemView;
            int bottom = rlItemNode.getPaddingBottom();
            int top = rlItemNode.getPaddingTop();
            int right = rlItemNode.getPaddingRight();
            int left = rlItemNode.getPaddingLeft();
            rlItemNode.setBackgroundResource(poolItem.isSelected() ? R.drawable.corner_radius_lighter_border_blue : R.drawable.corner_radius_lighter);
            rlItemNode.setPadding(left, top, right, bottom);

            // Options
            itemView.setOnClickListener(this);
            itemView.setClickable(itemsClickable);
        }

        @Override
        public void onClick(View view) {
            if (onSelectPoolListener != null) {
                int position = getAdapterPosition(); // gets item position
                if (position != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it
                    onSelectPoolListener.onSelectPool(poolItems.get(position));
                }
            }
        }
    }
}
