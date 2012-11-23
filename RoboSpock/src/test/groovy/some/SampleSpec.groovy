package some

import spock.lang.Specification

//import android.view.View
//import com.google.inject.Inject
//import com.qwilt.activity.NewUserActivity
//import com.qwilt.data.dao.AlbumDaoInterface
//import com.qwilt.db.model.Album
//import com.qwilt.net.WebInterface
//import com.qwilt.net.photos.BatchUploadRequest
//import org.junit.runner.RunWith
//import org.mockito.Mockito
//import spock.lang.Specification
//import com.qwilt.*
//
//import static org.junit.Assert.assertEquals
//import pl.polidea.robospock.RoboSputnik
//import pl.polidea.robospock.UseShadows
//import pl.polidea.robospock.RobolectricGuiceModules

//@RunWith(RoboSputnik)
//@UseShadows(MyActivityManagerShadow)
//@RobolectricGuiceModules([TestModules, DataModule])
class SampleSpec extends Specification {

//    NewUserActivity activity
//
//    @Inject AlbumDaoInterface albumDao
//    @Inject public RobolectricUtils utils;
//
//    @Inject WebInterface webInterface
//
//    def testWarningTextVisibility() {
//        given:
//        activity = new NewUserActivity();
//        activity.onCreate(null);
//
//        activity.passwordEt.setText("a");
//
//        when:
//        activity.okBtn.performClick();
//
//        then:
//        View.VISIBLE == activity.invalidLayout.visibility
//        activity.warningTextTv.getText() =~ "First name"
//        Mockito.verify(webInterface)
//    }
//
//    def "db test"() {
//        given:
//        Album[] albums;
//        final BatchUploadRequest request = new BatchUploadRequest();
//        albums = request
//                .parseResponse(getJson("batch_upload_many_albums.json"), BatchUploadRequest.AlbumWrapper.class)
//                .getAlbums()
//
//        int addedSize;
//        when:
//        addedSize = albumDao.create(Arrays.asList(albums));
//
//        then:
//        assertEquals(albums.length, addedSize);
//    }
//
//    def "check clean db"() {
//        expect:
//        albumDao.albums.isEmpty()
//    }
//
//    private InputStream getJson(final String name) throws IOException {
//        return utils.getStreamingJson("photos/" + name);
//    }

}

