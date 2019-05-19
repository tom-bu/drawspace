package draw.space;

import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by Tommy on 2/14/18.
 */

public class DrawnItem {
    Paint paint;
    Path path;

    public Path getPath(){
        return path;
    }

    public void setPath(Path path){
        this.path = path;
    }

    public Paint getPaint(){
        return paint;
    }

    public void setPaint(Paint paint){
        this.paint = paint;
    }
}
