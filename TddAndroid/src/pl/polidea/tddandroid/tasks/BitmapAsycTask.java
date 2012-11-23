package pl.polidea.tddandroid.tasks;

import java.io.File;

import pl.polidea.tddandroid.activity.MainActivity;
import pl.polidea.tddandroid.web.WebInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.inject.Inject;

public class BitmapAsycTask extends MyRoboAsycTask<Bitmap> {

    private final MainActivity mainActivity;
    @Inject WebInterface webInterface;
    private final String url;

    public BitmapAsycTask(final MainActivity mainActivity, final String url) {
        super(mainActivity);
        this.mainActivity = mainActivity;
        this.url = url;
    }

    @Override
    public Bitmap call() throws Exception {
        String path = mainActivity.getCacheDir().getPath() + "/image";
        final File file = webInterface.downloadFile(url, path);
        return BitmapFactory.decodeFile(file.getPath());
    }

    @Override
    public void onSuccess(final Bitmap t) throws Exception {
        super.onSuccess(t);
        mainActivity.setImageBitmap(t);
    }
}
