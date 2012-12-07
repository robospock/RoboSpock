package pl.polidea.tddandroid.activity;

import com.google.inject.Inject;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.shadows.ShadowBitmapFactory;
import junit.framework.Assert;
import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.polidea.tddandroid.MyTestRunner;
import pl.polidea.tddandroid.web.WebInterface;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.when;

@RunWith(MyTestRunner.class)
public class TaskActivityTest {
    private File FILE;
    @Inject
    WebInterface webInterface;

    @Before
    public void setup() throws Exception {
        FILE = new File(Robolectric.application.getCacheDir().getPath() + "/image");
    }

    @Test
    public void testAsyncText() {
        // given
        final TaskActivity taskActivity = new TaskActivity();
        taskActivity.onCreate(null);

        // when
        final CharSequence memory = taskActivity.asyncText.getText();

        // then
        Assert.assertEquals("WebText", memory);
    }

    @Test
    public void testWebText() throws IllegalStateException, ClientProtocolException, IOException {
        // given
        when(webInterface.execute("http://dev.polidea.pl/ext/szlif677557/text")).thenReturn("Hi! I'm text from ext :)");

        final TaskActivity taskActivity = new TaskActivity();
        taskActivity.onCreate(null);

        // when
        final CharSequence memory = taskActivity.webTv.getText();

        // then
        Assert.assertEquals("Hi! I'm text from ext :)", memory);
    }

    @Test
    public void testWebImage() throws ClientProtocolException, IOException {
        // given
        when(webInterface.downloadFile("http://www.polidea.pl/CorporateIdentity/logo_100x60.png", FILE.getPath()))
                .thenReturn(FILE);
        ShadowBitmapFactory.provideWidthAndHeightHints(FILE.getPath(), 200, 300);
        final TaskActivity taskActivity = new TaskActivity();
        taskActivity.onCreate(null);

        // when
        taskActivity.loadBtn.performClick();

        // then
        Assert.assertNotNull(taskActivity.webIv.getDrawable());
    }
}
