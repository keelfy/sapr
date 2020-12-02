package keelfy.sapr.dto;

import lombok.Getter;

/**
 * @author e.kuzmin
 */
@Getter
public enum SaprScene {

    MAIN_MENU,
    PRE_PROCESSOR,
    PROCESSOR,
    POST_PROCESSOR,
    CHOOSE_FILE;

    private final String fileName;
    private final String title;

    SaprScene() {
        this.fileName = this.toString().toLowerCase();
        final var lowerCase = fileName.replaceAll("_", " ");
        this.title = lowerCase.substring(0, 1).toUpperCase() + lowerCase.substring(1);
    }
}
