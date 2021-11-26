package io.taptalk.meettalkandroidsample.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.taptalk.TapTalk.Helper.TAPBaseViewHolder;
import io.taptalk.TapTalk.Helper.TAPUtils;
import io.taptalk.TapTalk.Helper.TapTalk;
import io.taptalk.TapTalk.Helper.recyclerview_fastscroll.views.FastScrollRecyclerView;
import io.taptalk.TapTalk.Model.TAPCountryRecycleItem;
import io.taptalk.TapTalk.View.Adapter.TAPBaseAdapter;
import io.taptalk.meettalkandroidsample.R;
import io.taptalk.meettalkandroidsample.activity.TAPCountryListActivity;

import static io.taptalk.TapTalk.Model.TAPCountryRecycleItem.RecyclerItemType.COUNTRY_INITIAL;

import com.bumptech.glide.Glide;

public class TAPCountryListAdapter extends TAPBaseAdapter<TAPCountryRecycleItem, TAPBaseViewHolder<TAPCountryRecycleItem>>
        implements FastScrollRecyclerView.SectionedAdapter, FastScrollRecyclerView.MeasurableAdapter<TAPBaseViewHolder<TAPCountryRecycleItem>> {

    private TAPCountryListActivity.TAPCountryPickInterface countryPickInterface;

    public TAPCountryListAdapter(List<TAPCountryRecycleItem> items, TAPCountryListActivity.TAPCountryPickInterface countryPickInterface) {
        setItems(items);
        this.countryPickInterface = countryPickInterface;
    }

    @NonNull
    @Override
    public TAPBaseViewHolder<TAPCountryRecycleItem> onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        switch (TAPCountryRecycleItem.RecyclerItemType.values()[i]) {
            case COUNTRY_INITIAL:
                return new CountryInitialViewHolder(viewGroup, R.layout.tap_country_initial_recycle_item); // TODO: 21 November 2019 USE CELL SECTION TITLE?
            default:
                return new CountryItemViewHolder(viewGroup, R.layout.tap_country_recycle_item);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return getItems().get(position).getRecyclerItemType().ordinal();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return getItemAt(position).getCountryInitial() + "";
    }

    @Override
    public int getViewTypeHeight(RecyclerView recyclerView, @Nullable TAPBaseViewHolder<TAPCountryRecycleItem> viewHolder, int viewType) {
        if (COUNTRY_INITIAL == TAPCountryRecycleItem.RecyclerItemType.values()[viewType]) {
            return TAPUtils.dpToPx(TapTalk.appContext.getResources(), 52);
        } else {
            return TAPUtils.dpToPx(TapTalk.appContext.getResources(), 44);
        }
    }

    public class CountryItemViewHolder extends TAPBaseViewHolder<TAPCountryRecycleItem> {
        private TextView tvCountryName;
        private ImageView ivCountryChoosen, ivCountryFlag;

        protected CountryItemViewHolder(ViewGroup parent, int itemLayoutId) {
            super(parent, itemLayoutId);
            tvCountryName = itemView.findViewById(R.id.tv_country_name);
            ivCountryChoosen = itemView.findViewById(R.id.iv_country_choosen);
            ivCountryFlag = itemView.findViewById(R.id.iv_country_flag);
        }

        @Override
        protected void onBind(TAPCountryRecycleItem item, int position) {
            tvCountryName.setText(item.getCountryListItem().getCommonName());

            Glide.with(itemView).load(item.getCountryListItem().getFlagIconUrl()).into(ivCountryFlag);

            itemView.setOnClickListener(v -> countryPickInterface.onPick(item.getCountryListItem()));

            if (item.isSelected()) {
                ivCountryChoosen.setVisibility(View.VISIBLE);
            } else {
                ivCountryChoosen.setVisibility(View.GONE);
            }
        }
    }

    public class CountryInitialViewHolder extends TAPBaseViewHolder<TAPCountryRecycleItem> {
        private TextView tvCountryInitial;

        CountryInitialViewHolder(ViewGroup parent, int itemLayoutId) {
            super(parent, itemLayoutId);
            tvCountryInitial = itemView.findViewById(R.id.tv_country_initial);
        }

        @Override
        protected void onBind(TAPCountryRecycleItem item, int position) {
            tvCountryInitial.setText(item.getCountryInitial() + "");
        }
    }
}
