package pdf;

import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.renderer.IRenderer;
import com.itextpdf.layout.renderer.TableRenderer;

public class ClippedTableRenderer extends TableRenderer {
    public ClippedTableRenderer(Table modelElement) {
        super(modelElement);
    }

//    @Override
//    public void draw(DrawContext drawContext) {
//        Rectangle tableRect = getOccupiedAreaBBox();
//        float tableContentHeight = calculateContentHeight();
//
//        System.out.println("tableContentHeight: " + tableContentHeight);
//        System.out.println("tableRect.getHeight(): " + tableRect.getHeight());
//
//        // Check if the table content is clipped
////        if (tableContentHeight > tableRect.getHeight()) {
////            System.out.println("TABLE CLIPPED");
////            throw new IllegalStateException("Table content is clipped");
////        }
//
//        super.draw(drawContext);
//    }

    @Override
    protected TableRenderer createOverflowRenderer(Table.RowRange rowRange) {
//        System.out.println("rowRange: " + rowRange.getStartRow() + "-" + rowRange.getFinishRow());
        throw new RuntimeException("El contenido no entra en las celdas. Disminuye el tamaño de la imagen o de las fuentes.");
    }

    @Override
    public IRenderer getNextRenderer() {
        return new ClippedTableRenderer((Table) modelElement);
    }

//    private float calculateContentHeight() {
//        float occupiedHeight = 0f;
//
//        for (IRenderer renderer : getChildRenderers()) {
//            occupiedHeight += renderer.getOccupiedArea().getBBox().getHeight();
//        }
//
//        return occupiedHeight;
//    }

}