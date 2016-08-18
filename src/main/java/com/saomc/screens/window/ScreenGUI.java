package com.saomc.screens.window;

import com.saomc.GLCore;
import com.saomc.SoundCore;
import com.saomc.api.events.ElementAction;
import com.saomc.api.screens.Actions;
import com.saomc.api.screens.GuiSelection;
import com.saomc.colorstates.CursorStatus;
import com.saomc.elements.Element;
import com.saomc.elements.ElementDispatcher;
import com.saomc.elements.ParentElement;
import com.saomc.resources.StringNames;
import com.saomc.util.ColorUtil;
import com.saomc.util.OptionCore;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public abstract class ScreenGUI extends GuiScreen implements ParentElement {

    private static final float ROTATION_FACTOR = 0.25F;
    protected static CursorStatus CURSOR_STATUS = CursorStatus.SHOW;
    protected final ElementDispatcher elements;
    private final Cursor emptyCursor;
    private GuiSelection type;
    private int mouseX, mouseY;
    private int mouseDown;
    private float mouseDownValue;
    private float[] rotationYaw, rotationPitch;
    private boolean cursorHidden = false;
    private boolean lockCursor = false;

    protected ScreenGUI(GuiSelection guiSelection) {
        super();
        type = guiSelection;
        elements = new ElementDispatcher(this, guiSelection);
        Cursor cursor = null;
        try {
            cursor = new Cursor(1, 1, 0, 0, 1, BufferUtils.createIntBuffer(1), null);
        } catch (LWJGLException e) {
            e.printStackTrace();
        } finally {
            emptyCursor = cursor;
        }
    }

    @Override
    public void initGui() {
        if (CURSOR_STATUS != CursorStatus.DEFAULT) hideCursor();

        super.initGui();
        init();
    }

    protected void init() {
        if (mc.thePlayer != null) {
            rotationYaw = new float[]{mc.thePlayer.rotationYaw};
            rotationPitch = new float[]{mc.thePlayer.rotationPitch};
        }
    }

    private int getCursorX() {
        if (OptionCore.CURSOR_TOGGLE.getValue()) return lockCursor ? 0 : (width / 2 - mouseX) / 2;
        else return !isCtrlKeyDown() ? (width / 2 - mouseX) / 2 : 0;
    }

    private int getCursorY() {
        if (OptionCore.CURSOR_TOGGLE.getValue()) return lockCursor ? 0 : (height / 2 - mouseY) / 2;
        else return !isCtrlKeyDown() ? (height / 2 - mouseY) / 2 : 0;
    }

    @Override
    public int getX(boolean relative) {
        return getCursorX();
    }

    @Override
    public int getY(boolean relative) {
        return getCursorY();
    }

    @Override
    public void updateScreen() {
        if (this.elements == null) return;

        this.elements.menuElements.values().forEach(e -> e.update(mc));
    }

    @Override
    public void drawScreen(int cursorX, int cursorY, float partialTicks) {
        if (this.elements == null) return;
        mouseX = cursorX;
        mouseY = cursorY;

        if (mc.thePlayer != null) {
            mc.thePlayer.rotationYaw = rotationYaw[0] - getCursorX() * ROTATION_FACTOR;
            mc.thePlayer.rotationPitch = rotationPitch[0] - getCursorY() * ROTATION_FACTOR;
        }

//        super.drawScreen(cursorX, cursorY, partialTicks); -> we might not want this to be called. Shouldn't have any effect ("empty" call)

        GLCore.glStartUI(mc);

        if (CURSOR_STATUS == CursorStatus.SHOW) {

            GLCore.glBlend(true);
            GLCore.tryBlendFuncSeparate(770, 771, 1, 0);
            GLCore.glBindTexture(OptionCore.SAO_UI.getValue() ? StringNames.gui : StringNames.guiCustom);

            if (mouseDown != 0) {
                final float fval = partialTicks * 0.1F;

                if (mouseDownValue + fval < 1.0F) mouseDownValue += fval;
                else mouseDownValue = 1.0F;

                GLCore.glColorRGBA(ColorUtil.CURSOR_COLOR.multiplyAlpha(mouseDownValue));
                GLCore.glTexturedRect(cursorX - 7, cursorY - 7, 35, 115, 15, 15);

                GLCore.glColorRGBA(ColorUtil.DEFAULT_COLOR);
            } else {
                mouseDownValue = 0;

                GLCore.glColorRGBA(ColorUtil.CURSOR_COLOR);
            }

            GLCore.glTexturedRect(cursorX - 7, cursorY - 7, 20, 115, 15, 15);
            GLCore.glBlend(false);
        }

        this.elements.menuElements.values().forEach(e -> e.draw(mc, cursorX, cursorY));

        GLCore.glEndUI(mc);
    }

    @Override
    protected void keyTyped(char ch, int key) throws IOException {
        if (OptionCore.CURSOR_TOGGLE.getValue() && isCtrlKeyDown()) lockCursor = !lockCursor;
        super.keyTyped(ch, key);

        elements.menuElements.keySet().stream().filter(Element::isFocus).forEach(element -> actionPerformed(element, Actions.KEY_TYPED, key));
    }

    // TODO: check the way elements is built... Breakpoint gives some weird result (at least for base menu)
    @Override
    protected void mouseClicked(int cursorX, int cursorY, int button) throws IOException {
        super.mouseClicked(cursorX, cursorY, button);
        mouseDown |= (0x1 << button);

        if (elements.menuElements.values().stream().filter(e -> e.mouseOver(cursorX, cursorY) && e.mousePressed(mc, cursorX, cursorY, button)).peek(e -> actionPerformed(e.getElement(), Actions.getAction(button, true), button)).count() == 0)
            backgroundClicked(cursorX, cursorY, button);
    }

    @Override
    protected void mouseReleased(int cursorX, int cursorY, int button) {
        super.mouseReleased(cursorX, cursorY, button);
        mouseDown &= ~(0x1 << button);

        elements.menuElements.values().stream().filter(e -> e.mouseOver(cursorX, cursorY) && e.mouseReleased(mc, cursorX, cursorY, button)).forEach(e -> actionPerformed(e.getElement(), Actions.getAction(button, false), button));
    }

    protected void backgroundClicked(int cursorX, int cursorY, int button) {
    }

    private void mouseWheel(int cursorX, int cursorY, int delta) {
        elements.menuElements.values().stream().filter(element -> element.mouseOver(cursorX, cursorY) && element.mouseWheel(mc, cursorX, cursorY, delta)).forEach(element -> actionPerformed(element.getElement(), Actions.MOUSE_WHEEL, delta));
    }

    public void actionPerformed(Element element, Actions action, int data) {
        MinecraftForge.EVENT_BUS.post(new ElementAction(element.getCaption(), element.getCategory(), action, data, element.getGui(), element.isFocus()));
        element.setFocus(!element.isFocus());
        SoundCore.play(mc.getSoundHandler(), SoundCore.DIALOG_CLOSE);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        if (Mouse.hasWheel()) {
            final int x = Mouse.getEventX() * width / mc.displayWidth;
            final int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            final int delta = Mouse.getEventDWheel();

            if (delta != 0) mouseWheel(x, y, delta);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return OptionCore.GUI_PAUSE.getValue();
    }

    @Override
    public void onGuiClosed() {
        showCursor();

        close();
    }

    protected void close() {
        elements.menuElements.clear(); // jic
    }

    protected void hideCursor() {
        if (!cursorHidden) toggleHideCursor();
    }

    protected void showCursor() {
        if (cursorHidden) toggleHideCursor();
    }

    protected void toggleHideCursor() {
        cursorHidden = !cursorHidden;
        try {
            Mouse.setNativeCursor(cursorHidden ? emptyCursor : null);
        } catch (LWJGLException ignored) {
        }
    }
}
