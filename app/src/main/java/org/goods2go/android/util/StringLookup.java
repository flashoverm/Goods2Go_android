package org.goods2go.android.util;

import com.goods2go.models.enums.IdentType;
import com.goods2go.models.enums.NotificationMessageType;
import com.goods2go.models.enums.PaymentType;
import com.goods2go.models.enums.Role;
import com.goods2go.models.enums.Status;

import org.goods2go.android.R;

public class StringLookup {

    public static int getIdFrom(IdentType identType){
        switch (identType){
            case PASSPORT:
                return R.string.PASSPORT;
            case ID:
                return R.string.ID;
            case DRIVERLICENSE:
                return R.string.DRIVERLICENSE;
        }
        return -1;
    }

    public static int getIdFrom(PaymentType paymentType){
        switch (paymentType){
            case BANKTRANSFER:
                return R.string.BANKTRANSFER;
            case PAYPAL:
                return R.string.PAYPAL;
        }
        return -1;
    }

    public static int getIdFrom(Role role){
        switch (role){
            case SENDER:
                return R.string.SENDER;
            case DELIVERER:
                return R.string.DELIVERER;
            case ADMIN:
                return R.string.ADMIN;
            case NOTACTIVE:
                return R.string.NOACTIVE;
        }
        return -1;
    }

    public static int getIdFrom(Status status){
        switch (status){
            case ANNOUNCED:
                return R.string.ANNOUNCED;
            case PENDING:
                return R.string.PENDING;
            case ACTIVE:
                return R.string.ACTIVE;
            case DELIVERED:
                return R.string.DELIVERED;
            case PAIDANDSENDERRATED:
                return R.string.PAIDANDSENDERRATED;
            case DELIVERERRATED:
                return R.string.DELIVERERRATED;
        }
        return -1;
    }

    public static int getIdFrom(NotificationMessageType type){
        switch (type){
            case NewRequest:
                return R.string.NewRequest;
            case RequestAccepted:
                return R.string.RequestAccepted;
            case RequestDeclined:
                return R.string.RequestDeclined;
            case ShipmentPickedUp:
                return R.string.ShipmentPickedUp;
            case ShipmentDelivered:
                return R.string.ShipmentDelivered;
            case ShipmentSenderRated:
                return R.string.ShipmentSenderRated;
            case ShipmentDelivererRated:
                return R.string.ShipmentDelivererRated;
            case FittingAnnouncement:
                return R.string.FittingAnnouncement;
            case Test:
                return R.string.Test;
        }
        return -1;
    }
}
