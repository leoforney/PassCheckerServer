package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Div;
import org.springframework.lang.NonNull;

@HtmlImport("bower_components/granite-qrcode-generator/granite-qrcode-generator.html")
@Tag("granite-qrcode-generator")
public class GraniteQRCode extends Div {

    public GraniteQRCode() {
        setMode(Mode.OCTET);
        setData("");
        generateQRCode();
    }

    public GraniteQRCode(String data) {
        setMode(Mode.OCTET);
        setData(data);
        generateQRCode();
    }

    public void setMode(@NonNull Mode mode) {
        System.out.println(mode.toString());
        getElement().setProperty("mode", mode.toString().toLowerCase());
        paramsChanged();
    }

    public void setData(String value) {
        getElement().setProperty("data", value);
        paramsChanged();
    }

    public String getData() {
        return getElement().getProperty("data");
    }

    public void setAuto(boolean auto) {
        getElement().setProperty("auto", auto);
        paramsChanged();
    }

    public boolean getAuto() {
        return Boolean.parseBoolean(getElement().getProperty("auto"));
    }

    public void generateQRCode() {
        getElement().callFunction("generateQRCode");
    }

    public void paramsChanged() {
        getElement().callFunction("paramsChanged");
    }

    public enum Mode {
        NUMERIC,
        ALPHANUMERIC,
        OCTET
    }

}
