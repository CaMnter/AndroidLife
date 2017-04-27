/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.camnter.newlife.core.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

/**
 * Non-UI Fragment used to retain ViewModels.
 */
public class ViewModelHolder<VM> extends Fragment {

    private VM vm;


    public ViewModelHolder() { }


    public static <M> ViewModelHolder createContainer(@NonNull M viewModel) {
        ViewModelHolder<M> viewModelContainer = new ViewModelHolder<>();
        viewModelContainer.setViewModel(viewModel);
        return viewModelContainer;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @Nullable public VM getViewModel() {
        return this.vm;
    }


    public void setViewModel(@NonNull VM vm) {
        this.vm = vm;
    }

}
