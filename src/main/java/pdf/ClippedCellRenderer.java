package pdf;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.renderer.CellRenderer;
import com.itextpdf.layout.renderer.DrawContext;
import com.itextpdf.layout.renderer.IRenderer;

public class ClippedCellRenderer extends CellRenderer {
    public ClippedCellRenderer(Cell modelElement) {
        super(modelElement);
    }

    @Override
    public void draw(DrawContext drawContext) {
        Rectangle cellRect = getOccupiedAreaBBox();
        float cellContentHeight = calculateContentHeight();

        System.out.println("cellContentHeight: " + cellContentHeight);
        System.out.println("cellRect.getHeight(): " + cellRect.getHeight());

        // Check if the cell content is clipped
        if (cellContentHeight > cellRect.getHeight()) {
            System.out.println("CELL CLIPPED");
            throw new IllegalStateException("Cell content is clipped");
        }

        super.draw(drawContext);
    }

    @Override
    public IRenderer getNextRenderer() {
        return new ClippedCellRenderer((Cell) modelElement);
    }

    private float calculateContentHeight() {
        float occupiedHeight = 0f;

        for (IRenderer renderer : getChildRenderers()) {
            occupiedHeight += renderer.getOccupiedArea().getBBox().getHeight();
        }

        return occupiedHeight;
    }

}
