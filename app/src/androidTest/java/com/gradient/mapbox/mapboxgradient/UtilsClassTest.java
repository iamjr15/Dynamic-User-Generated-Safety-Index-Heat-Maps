package com.gradient.mapbox.mapboxgradient;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;


public class UtilsClassTest {

    @Test
    public void isPhoneValidTest() {

        // Bad
        assertThat(Utils.isPhoneValid("861680545"), is(false));
        assertThat(Utils.isPhoneValid("+89454ds"), is(false));
        assertThat(Utils.isPhoneValid("+3"), is(false));
        assertThat(Utils.isPhoneValid("+39684564894356454564654654654654654654"), is(false));
        assertThat(Utils.isPhoneValid("++654654654"), is(false));
        assertThat(Utils.isPhoneValid("+654/54564654"), is(false));


        // Good
        assertThat(Utils.isPhoneValid("+370(657)654645"), is(true));
        assertThat(Utils.isPhoneValid("+39654561"), is(true));
        assertThat(Utils.isPhoneValid("+370(61)607381"), is(true));
        assertThat(Utils.isPhoneValid("+370-564654-381"), is(true));
    }


    @Test
    public void isEmailValidTest() {

        // Bad emails
        assertThat(Utils.isEmailValid("sdfg"), is(false));
        assertThat(Utils.isEmailValid("sdfg@"), is(false));
        assertThat(Utils.isEmailValid("sdfg@654"), is(false));
        assertThat(Utils.isEmailValid("sdfg@.lt"), is(false));
        assertThat(Utils.isEmailValid("@gmail.lt"), is(false));
        assertThat(Utils.isEmailValid("asdasd.lt"), is(false));
        assertThat(Utils.isEmailValid("as@da@sd.lt"), is(false));
        assertThat(Utils.isEmailValid(".name.surname@domain.lt"), is(false));


        // Good emails
        assertThat(Utils.isEmailValid("name.surname@domain.lt"), is(true));
        assertThat(Utils.isEmailValid("asd@asdf.lt"), is(true));
        assertThat(Utils.isEmailValid("asd@sub.domain.lt"), is(true));
        assertThat(Utils.isEmailValid("as-some_symbolsd@sub.domain.lt"), is(true));
    }
}
