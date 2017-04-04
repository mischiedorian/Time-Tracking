package com.dorian.licenta;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Created by misch on 04.04.2017.
 */

class DemoJobCreator implements JobCreator {

    @Override
    public Job create(String tag) {
        switch (tag) {
            case ShowNotification.TAG:
                return new ShowNotification();
            default:
                return null;
        }
    }
}