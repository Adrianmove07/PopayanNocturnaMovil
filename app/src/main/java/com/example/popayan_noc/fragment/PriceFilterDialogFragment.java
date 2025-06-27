// PriceFilterDialogFragment.java
package com.example.popayan_noc.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class PriceFilterDialogFragment extends DialogFragment {

    public interface PriceFilterListener {
        void onPriceFilterSelected(String priceRange);
    }

    private PriceFilterListener listener;

    public void setPriceFilterListener(PriceFilterListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String[] priceRanges = {"gratis", "economico", "medio", "premium", "todos"}; // "todos" for no filter
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Filtrar por Precio")
                .setItems(priceRanges, (dialog, which) -> {
                    String selectedRange = priceRanges[which];
                    if (listener != null) {
                        listener.onPriceFilterSelected(selectedRange);
                    }
                });
        return builder.create();
    }
}