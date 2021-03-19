package org.goods2go.android.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goods2go.models.Address;

import org.goods2go.android.R;

import static org.goods2go.android.Configuration.SHOW_COUNTRY;

public class AddressView extends RelativeLayout{

    private View view;


    public AddressView(Context context) {
        super(context);
        view = inflate(context, R.layout.layout_address, this);
    }

    public AddressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        view = inflate(context, R.layout.layout_address, this);

        TypedArray attributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.AddressView,
                0, 0);

        TextView descriptor = view.findViewById(R.id.address_descriptor);
        descriptor.setText(attributes.getString(R.styleable.AddressView_descriptor));

        descriptor.setTextSize(TypedValue.COMPLEX_UNIT_PX, attributes.getDimension(
                R.styleable.AddressView_descriptor_size, new TextView(getContext()).getTextSize())
        );
        if(attributes.getBoolean(R.styleable.AddressView_descriptor_bold, false)){
            descriptor.setTypeface(null, Typeface.BOLD);
        }
    }

    public static Address censorAddress(Address address){
        address.setCompanyname(null);
        address.setFirstname(null);
        address.setLastname(null);
        address.setStreetno(null);
        return address;
    }

    public void setAddress(Address address){
        LinearLayout addressLayout = view.findViewById(R.id.address_layout);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(ALIGN_PARENT_END);
        addressLayout.setLayoutParams(params);
        setTightAddress(address);
    }

    public void setTightAddress(Address address) {
        if(address.getFirstname() == null || address.getFirstname().equals("")
                || address.getFirstname() == null || address.getFirstname().equals("")) {
            LinearLayout name = view.findViewById(R.id.address_name);
            name.setVisibility(GONE);
        } else {
            TextView firstname = view.findViewById(R.id.address_firstname);
            TextView lastname = view.findViewById(R.id.address_lastname);
            firstname.setText(address.getFirstname());
            lastname.setText(address.getLastname());
        }

        TextView streetno = view.findViewById(R.id.address_streetno);
        if(address.getStreetno() == null || address.getStreetno().equals("")){
            streetno.setVisibility(GONE);
        } else {
            streetno.setText(address.getStreetno());
        }

        TextView street = view.findViewById(R.id.address_street);
        if(address.getStreet() == null || address.getStreet().equals("")){
            street.setVisibility(GONE);
        } else {
            street.setText(address.getStreet());
        }

        TextView postcode = view.findViewById(R.id.address_postcode);
        TextView city = view.findViewById(R.id.address_city);
        postcode.setText(address.getPostcode());
        city.setText(address.getCity());

        TextView country = view.findViewById(R.id.address_country);
        if(SHOW_COUNTRY && address.getStreetno() != null && !address.getStreetno().equals("")){
            country.setText(address.getCountry());
        } else {
            country.setVisibility(GONE);
        }
    }
}
