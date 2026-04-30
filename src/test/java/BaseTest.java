import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.testng.annotations.*;
import org.testng.Assert;
import java.time.Duration;
import java.util.List;

public class BaseTest {
    private static final long STEP_DELAY_MS = 1200;
    private static final long FINAL_DELAY_MS = 4000;
    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeMethod
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get("http://localhost:5500");
        login("nguyenthian", "12345678");
    }

    public void login(String user, String pass) {
        driver.findElement(By.cssSelector("[data-page='login-page']")).click();
        driver.findElement(By.id("username")).sendKeys(user);
        driver.findElement(By.id("pwd")).sendKeys(pass);
        driver.findElement(By.id("login-agree")).click();
        driver.findElement(By.cssSelector("button[onclick='handleLogin()']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nav-user")));
        closeModalOverlayIfPresent();
        freezeAutoNavigation();
    }

    protected void freezeAutoNavigation() {
        ((JavascriptExecutor) driver).executeScript(
                "if (!window.__originalSetTimeout) {" +
                "  window.__originalSetTimeout = window.setTimeout;" +
                "}" +
                "window.setTimeout = function(fn, delay) {" +
                "  if (delay > 0) return 0;" +
                "  return window.__originalSetTimeout(fn, delay);" +
                "};" +
                "if (window.UI && !window.__originalUINavigate) {" +
                "  window.__originalUINavigate = window.UI.navigate.bind(window.UI);" +
                "}" +
                "if (window.UI) {" +
                "  window.UI.navigate = function(pageId) {" +
                "    if (pageId === 'home-page' || pageId === 'list-page') { return; }" +
                "    return window.__originalUINavigate(pageId);" +
                "  };" +
                "}" +
                "if (!window.__originalNavigate) {" +
                "  window.__originalNavigate = window.navigate;" +
                "}" +
                "window.navigate = function(pageId) {" +
                "  if (pageId === 'home-page' || pageId === 'list-page') { return; }" +
                "  return window.__originalNavigate(pageId);" +
                "};"
        );
    }

    protected void closeModalOverlayIfPresent() {
        By overlay = By.id("modal-overlay");
        try {
            WebElement overlayElement = wait.until(ExpectedConditions.visibilityOfElementLocated(overlay));
            for (By locator : new By[] {
                    By.cssSelector("#modal-overlay button"),
                    By.cssSelector("#modal button"),
                    By.cssSelector(".modal button")
            }) {
                if (clickIfPresent(locator)) {
                    break;
                }
            }

            wait.until(ExpectedConditions.invisibilityOf(overlayElement));
        } catch (TimeoutException ignored) {
            // No modal was shown after login.
        }
    }

    protected void clickWhenReady(By locator) {
        WebElement element = findVisibleElement(locator);
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        } catch (TimeoutException | ElementNotInteractableException ignored) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
        pause(STEP_DELAY_MS);
    }

    protected void setFieldValue(By locator, String value) {
        WebElement element = findVisibleElement(locator);
        try {
            wait.until(ExpectedConditions.visibilityOf(element));
            element.clear();
            element.sendKeys(value);
        } catch (InvalidElementStateException | TimeoutException ignored) {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = arguments[1];" +
                            "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
                            "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                    element,
                    value
            );
        }
        pause(STEP_DELAY_MS);
    }

    protected void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Test execution was interrupted", e);
        }
    }

    private WebElement findVisibleElement(By locator) {
        return wait.until(driver -> {
            List<WebElement> elements = driver.findElements(locator);
            for (WebElement element : elements) {
                if (element.isDisplayed()) {
                    return element;
                }
            }
            return null;
        });
    }

    private boolean clickIfPresent(By locator) {
        try {
            WebElement element = findVisibleElement(locator);
            if (element.isDisplayed() && element.isEnabled()) {
                element.click();
                return true;
            }
        } catch (NoSuchElementException | ElementNotInteractableException | TimeoutException ignored) {
            return false;
        }
        return false;
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            pause(FINAL_DELAY_MS);
            driver.quit();
        }
    }
}
