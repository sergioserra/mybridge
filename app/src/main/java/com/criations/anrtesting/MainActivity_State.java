package com.criations.anrtesting;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Sérgio Serra on 04/08/2018.
 * Criations
 * sergioserra99@gmail.com
 */
public class MainActivity_State implements Parcelable{

    @SuppressWarnings("UnusedAssignment")
    private String state1 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque ac nulla lacinia, gravida est sit amet, gravida est. Sed elit mauris, rutrum vitae orci dignissim, consequat lacinia nisl. Vivamus luctus ipsum eu tortor vehicula, eget sagittis metus sagittis. Proin convallis tincidunt neque, vitae pellentesque augue vestibulum nec. Curabitur lacinia, purus finibus lacinia dictum, velit lacus sollicitudin mi, laoreet iaculis massa orci ut ipsum. Praesent quis viverra nunc. Curabitur sodales arcu porttitor odio aliquam ultricies. Cras viverra massa scelerisque massa rhoncus, in fermentum diam elementum. Mauris non urna eget risus bibendum venenatis nec at metus. Donec porta, diam ut sagittis laoreet, urna risus placerat libero, sit amet faucibus justo ex eu lectus. Pellentesque pretium a ligula ac sollicitudin.\n" +
            "\n" +
            "Maecenas in tellus in nisi consectetur pharetra non sit amet nunc. Curabitur ac accumsan odio. Sed non metus vestibulum, ullamcorper quam a, porttitor nisi. Curabitur vel lacinia arcu. Etiam sed blandit tortor. Cras rhoncus nunc ut lorem laoreet congue. Etiam imperdiet ex a velit tempus elementum.\n" +
            "\n" +
            "Vestibulum ullamcorper sollicitudin ornare. In rutrum erat nec tellus dignissim sodales ac vel lacus. Pellentesque accumsan dui non condimentum viverra. Praesent porta viverra ex id posuere. Sed vitae nibh eget elit malesuada vulputate ac a dolor. Vivamus dignissim velit eu mi blandit, ac tristique augue rutrum. Vestibulum convallis ligula at facilisis consequat. Sed quam mi, imperdiet et ligula vel, iaculis tempus est.\n" +
            "\n" +
            "Etiam facilisis, quam a euismod dignissim, diam mauris vestibulum sem, ut hendrerit tortor sapien in quam. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc a molestie magna, eu scelerisque lectus. Nam blandit a urna sit amet fringilla. Nunc gravida velit lacus, non tincidunt urna fringilla sed. Maecenas volutpat, massa at semper laoreet, eros enim tristique risus, ac congue odio tellus id urna. Aenean urna enim, faucibus eget tortor eget, placerat gravida quam. Etiam vitae quam metus. Interdum et malesuada fames ac ante ipsum primis in faucibus.\n" +
            "\n" +
            "Curabitur elit felis, fermentum vel velit at, imperdiet molestie lectus. Sed tincidunt ligula non lacus scelerisque, non posuere diam efficitur. Nullam semper nunc felis, ac ullamcorper tellus lobortis eleifend. Sed nec mollis quam, at gravida tellus. Donec eleifend purus non urna dictum ullamcorper. Sed eros augue, maximus sed purus ac, elementum laoreet nulla. Vestibulum id tincidunt sem, at gravida sapien.";


    public String getState1() {
        return state1;
    }

    public MainActivity_State() {
    }

    private MainActivity_State(Parcel in) {
        state1 = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(state1);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MainActivity_State> CREATOR = new Parcelable.Creator<MainActivity_State>() {
        @Override
        public MainActivity_State createFromParcel(Parcel in) {
            return new MainActivity_State(in);
        }

        @Override
        public MainActivity_State[] newArray(int size) {
            return new MainActivity_State[size];
        }
    };

}

