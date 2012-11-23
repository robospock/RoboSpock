package pl.polidea.tddandroid.activity;

import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import junit.framework.Assert;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;

import pl.polidea.tddandroid.MyTestRunner;
import pl.polidea.tddandroid.database.DatabaseHelper;
import pl.polidea.tddandroid.database.DatabaseObject;
import pl.polidea.tddandroid.web.WebInterface;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Button;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.shadows.ShadowAlertDialog;
import com.xtremelabs.robolectric.shadows.ShadowBitmapFactory;
import com.xtremelabs.robolectric.shadows.ShadowDialog;

@RunWith(MyTestRunner.class)
public class MainActivityTest {
    private File FILE;
    @Inject WebInterface webInterface;

    @Inject DatabaseHelper databaseHelper;
    Dao<DatabaseObject, Integer> dao;

    @Before
    public void setup() throws Exception {

        Class.forName("org.sqlite.SQLite").newInstance();

        FILE =  new File(Robolectric.application.getCacheDir().getPath() + "/image");
        dao = databaseHelper.getDao(DatabaseObject.class);
        dao.create(new DatabaseObject("test", 4, 1));
        dao.create(new DatabaseObject("tset", 4, 2));
        dao.create(new DatabaseObject("testtset", 8, 3));
    }

    @Test
    public void testHelloText() {
        // given
        final MainActivity mainActivity = new MainActivity();
        mainActivity.onCreate(null);

        // when
        final CharSequence text = mainActivity.helloTv.getText();

        // then
        Assert.assertEquals("Hello Szlif!", text);
    }

    @Test
    public void testButtonClicked() {
        // given
        final MainActivity mainActivity = new MainActivity();
        mainActivity.onCreate(null);

        // when
        final Button button = mainActivity.button;
        button.performClick();
        final CharSequence text = mainActivity.buttonTv.getText();

        // then
        Assert.assertEquals("Clicked !", text);
    }

    @Test
    public void testCacheSizeText() {
        // given
        final MainActivity mainActivity = new MainActivity();
        mainActivity.onCreate(null);

        // when
        final CharSequence memory = mainActivity.memoryTv.getText();

        // then
        Assert.assertEquals("I have 16 MB", memory);
    }

    @Test
    public void testAsyncText() {
        // given
        final MainActivity mainActivity = new MainActivity();
        mainActivity.onCreate(null);

        // when
        final CharSequence memory = mainActivity.asyncText.getText();

        // then
        Assert.assertEquals("WebText", memory);
    }

    @Test
    public void testWebText() throws IllegalStateException, ClientProtocolException, IOException {
        // given
        when(webInterface.execute("http://dev.polidea.pl/ext/szlif677557/text")).thenReturn("Hi! I'm text from ext :)");

        final MainActivity mainActivity = new MainActivity();
        mainActivity.onCreate(null);

        // when
        final CharSequence memory = mainActivity.webTv.getText();

        // then
        Assert.assertEquals("Hi! I'm text from ext :)", memory);
    }

    @Test
    public void testWebImage() throws ClientProtocolException, IOException {
        // given
        when(webInterface.downloadFile("http://www.polidea.pl/CorporateIdentity/logo_100x60.png", FILE.getPath()))
                .thenReturn(FILE);
        ShadowBitmapFactory.provideWidthAndHeightHints(FILE.getPath(), 200, 300);
        final MainActivity mainActivity = new MainActivity();
        mainActivity.onCreate(null);

        // when
        mainActivity.loadBtn.performClick();

        // then
        Assert.assertNotNull(mainActivity.webIv.getDrawable());
    }

    @Test
    public void testDialogContent() {
        // given
        final MainActivity mainActivity = new MainActivity();
        mainActivity.onCreate(null);

        // when
        mainActivity.button.performClick();

        // then
        final ShadowAlertDialog dialog = (ShadowAlertDialog) Robolectric.shadowOf(ShadowDialog.getLatestDialog());
        Assert.assertEquals("title", dialog.getTitle());
        Assert.assertEquals("Ok", dialog.getButton(AlertDialog.BUTTON_POSITIVE).getText());
        Assert.assertEquals("Cancel", dialog.getButton(AlertDialog.BUTTON_NEGATIVE).getText());
        Assert.assertEquals("Dismiss", dialog.getButton(AlertDialog.BUTTON_NEUTRAL).getText());
        Assert.assertEquals("Dialog Content", dialog.getMessage());
    }

    @Test
    public void testDialogActions() {
        // given
        final MainActivity mainActivity = new MainActivity();
        final DialogInterface.OnClickListener mock = Mockito.mock(DialogInterface.OnClickListener.class);
        mainActivity.dialogListener = mock;
        mainActivity.onCreate(null);
        // Mockito.when(mock.onClick(Matchers.any(), Matchers.anyInt()));
        mainActivity.button.performClick();

        // when
        final ShadowAlertDialog dialog = (ShadowAlertDialog) Robolectric.shadowOf(ShadowDialog.getLatestDialog());
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).performClick();
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).performClick();

        // then
        Mockito.verify(mock, Mockito.times(3)).onClick((DialogInterface) Matchers.any(), Matchers.anyInt());
    }

    @Test
    public void testInsertingNewObjectToDB() throws SQLException {
        // given
        final DatabaseObject databaseObject = new DatabaseObject("title", 4, 0);

        // when
        final int insertSize = dao.create(databaseObject);

        // then
        Assert.assertEquals(1, insertSize);
    }

    // trololo, robolectric throw RE instead of real SQLException
    @Test(expected = RuntimeException.class)
    public void testBreakingSqlConstraint() throws SQLException {
        // given
        final DatabaseObject databaseObject = new DatabaseObject("title", 4, 1);

        // when
        dao.create(databaseObject);

        // then
        // exeption thrown

    }
}
