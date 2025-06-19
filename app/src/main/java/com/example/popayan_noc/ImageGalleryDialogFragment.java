package com.example.popayan_noc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;
import java.util.ArrayList;
import java.util.List;

public class ImageGalleryDialogFragment extends DialogFragment {
    private static final String ARG_IMAGES = "image_urls";
    private ArrayList<String> imageUrls;
    private int currentPosition = 0;

    // Si tienes descripciones, puedes recibirlas aquí. Por ahora, solo URLs.
    // private ArrayList<String> imageDescriptions;

    public static ImageGalleryDialogFragment newInstance(ArrayList<String> imageUrls) {
        ImageGalleryDialogFragment fragment = new ImageGalleryDialogFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_IMAGES, imageUrls);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_image_gallery, container, false);
        ViewPager2 viewPager = view.findViewById(R.id.viewPagerImages);
        ImageButton btnClose = view.findViewById(R.id.btnCloseGallery);
        ImageButton btnShare = view.findViewById(R.id.btnShareImage);
        TabLayout dotsIndicator = view.findViewById(R.id.galleryDots);
        TextView tvDescription = view.findViewById(R.id.galleryDescription);

        if (getArguments() != null) {
            imageUrls = getArguments().getStringArrayList(ARG_IMAGES);
        }
        if (imageUrls == null) imageUrls = new ArrayList<>();

        ImagePagerAdapter adapter = new ImagePagerAdapter(requireContext(), imageUrls);
        viewPager.setAdapter(adapter);
        new TabLayoutMediator(dotsIndicator, viewPager, (tab, position) -> {
            // No label for dots
        }).attach();

        // Descripción/título de la imagen (por ahora solo el nombre del archivo o la URL)
        if (!imageUrls.isEmpty()) {
            tvDescription.setText(getDescriptionForImage(0));
        }
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
                tvDescription.setText(getDescriptionForImage(position));
            }
        });

        btnClose.setOnClickListener(v -> dismiss());

        btnShare.setOnClickListener(v -> {
            if (!imageUrls.isEmpty() && currentPosition < imageUrls.size()) {
                String url = imageUrls.get(currentPosition);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, url);
                try {
                    startActivity(Intent.createChooser(shareIntent, "Compartir imagen"));
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "No se pudo compartir la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    // Puedes personalizar esto para usar descripciones reales si tu modelo lo soporta
    private String getDescriptionForImage(int position) {
        if (imageUrls == null || imageUrls.isEmpty()) return "";
        String url = imageUrls.get(position);
        // Ejemplo: solo el nombre del archivo
        try {
            return url.substring(url.lastIndexOf("/") + 1);
        } catch (Exception e) {
            return url;
        }
    }

    @Override
    public int getTheme() {
        // Puedes personalizar la animación aquí
        return android.R.style.Theme_Black_NoTitleBar_Fullscreen;
    }
}
