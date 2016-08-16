package com.saomc.screens.window;

import com.saomc.GLCore;
import com.saomc.SoundCore;
import com.saomc.api.events.ElementAction;
import com.saomc.api.screens.Actions;
import com.saomc.api.screens.GuiSelection;
import com.saomc.colorstates.CursorStatus;
import com.saomc.elements.ElementBuilder;
import com.saomc.elements.ElementDispatcher;
import com.saomc.elements.Elements;
import com.saomc.elements.ParentElement;
import com.saomc.resources.StringNames;
import com.saomc.util.ColorUtil;
import com.saomc.util.OptionCore;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;

@SideOnly(Side.CLIENT)
public abstract class ScreenGUI extends GuiScreen implements ParentElement {

    private static final float ROTATION_FACTOR = 0.25F;
    protected static CursorStatus CURSOR_STATUS = CursorStatus.SHOW;
    protected final List<Elements> elements;
    private final Cursor emptyCursor;
    private GuiSelection gui;
    private int mouseX, mouseY;
    private int mouseDown;
    private float mouseDownValue;
    private float[] rotationYaw, rotationPitch;
    private boolean cursorHidden = false;
    private boolean lockCursor = false;

    protected ScreenGUI(GuiSelection guiSelection) {
        super();
        gui = guiSelection;
        elements = ElementBuilder.getInstance().getforGui(guiSelection);
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
        elements.clear();
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
        else return !super.isCtrlKeyDown() ? (width / 2 - mouseX) / 2 : 0;
    }

    private int getCursorY() {
        if (OptionCore.CURSOR_TOGGLE.getValue()) return lockCursor ? 0 : (height / 2 - mouseY) / 2;
        else return !super.isCtrlKeyDown() ? (height / 2 - mouseY) / 2 : 0;
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
        for (int i = elements.size() - 1; i >= 0; i--) {
            if (!elements.get(i).isEnabled()) {
                elements.remove(i);
                continue;
            }

            //elements.get(i).update(mc);
        }
    }

    @Override
    public void drawScreen(int cursorX, int cursorY, float f) {
        if (this.elements == null) return;
        for (Elements el : this.elements) if (el == null) return;
        mouseX = cursorX;
        mouseY = cursorY;

        if (mc.thePlayer != null) {
            mc.thePlayer.rotationYaw = rotationYaw[0] - getCursorX() * ROTATION_FACTOR;
            mc.thePlayer.rotationPitch = rotationPitch[0] - getCursorY() * ROTATION_FACTOR;
        }

        super.drawScreen(cursorX, cursorY, f);

        GLCore.glStartUI(mc);

        if (CURSOR_STATUS == CursorStatus.SHOW) {

            GLCore.glBlend(true);
            GLCore.tryBlendFuncSeparate(770, 771, 1, 0);
            GLCore.glBindTexture(OptionCore.SAO_UI.getValue() ? StringNames.gui : StringNames.guiCustom);


            if (mouseDown != 0) {
                final float fval = f * 0.1F;

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
        GLCore.glEndUI(mc);
    }

    @Override
    protected void keyTyped(char ch, int key) throws IOException {
        if (OptionCore.CURSOR_TOGGLE.getValue() && super.isCtrlKeyDown()) lockCursor = !lockCursor;
        super.keyTyped(ch, key);

        elements.stream().filter(element -> element.isFocus()).forEach(element -> actionPerformed(element, Actions.KEY_TYPED, key));
    }

    // TODO: check the way elements is built... Breakpoint gives some weird result (at least for base menu)
    @Override
    protected void mouseClicked(int cursorX, int cursorY, int button) throws IOException {
        super.mouseClicked(cursorX, cursorY, button);
        mouseDown |= (0x1 << button);

        boolean clickedElement = false;

        for (int i = elements.size() - 1; i >= 0; i--) {
            if (i >= elements.size()) {
                if (elements.size() > 0) i = elements.size() - 1;
                else break;
            }

            if (ElementDispatcher.menuElements.get(elements.get(i)).mouseOver(cursorX, cursorY)) {
                if (ElementDispatcher.menuElements.get(elements.get(i)).mousePressed(mc, cursorX, cursorY, button))
                    actionPerformed(elements.get(i), Actions.getAction(button, true), button);

                clickedElement = true;
            }
        }

        if (!clickedElement) backgroundClicked(cursorX, cursorY, button);
    }

    @Override
    protected void mouseReleased(int cursorX, int cursorY, int button) {
        super.mouseReleased(cursorX, cursorY, button);
        mouseDown &= ~(0x1 << button);

        for (int i = elements.size() - 1; i >= 0; i--) {
            if (i >= elements.size()) {
                if (elements.size() > 0) i = elements.size() - 1;
                else break;
            }
            if (ElementDispatcher.menuElements.get(elements.get(i)).mouseOver(cursorX, cursorY, button) && ElementDispatcher.menuElements.get(elements.get(i)).mouseReleased(mc, cursorX, cursorY, button))
                actionPerformed(elements.get(i), Actions.getAction(button, false), button);
        }
    }

    protected void backgroundClicked(int cursorX, int cursorY, int button) {
    }

    private void mouseWheel(int cursorX, int cursorY, int delta) {
        elements.stream().filter(element -> ElementDispatcher.menuElements.get(element).mouseOver(cursorX, cursorY) && ElementDispatcher.menuElements.get(element).mouseWheel(mc, cursorX, cursorY, delta)).forEach(element -> actionPerformed(element, Actions.MOUSE_WHEEL, delta));
    }

    public void actionPerformed(Elements element, Actions action, int data) {
        new ElementAction(element.getCaption(), element.getCategory(), action, data);
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
        elements.clear();
        ElementDispatcher.menuElements.clear();
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
