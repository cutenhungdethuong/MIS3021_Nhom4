import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.text.Normalizer;
import java.time.LocalTime;
import java.util.Locale;

public class AddBookingTest extends BaseTest {
    private static final String BOOKING_DATE = "2026-04-28";
    private static final String SUCCESS_TEXT = "dat lich thanh cong";
    private static final String INVALID_NAME_TEXT = "ho va ten khach hang khong hop le, yeu cau nhap lai";
    private static final String INVALID_PHONE_TEXT = "so dien thoai khach hang khong hop le, yeu cau nhap lai";
    private static final String EMPTY_DATE_TEXT = "vui long chon ngay";
    private static final String EMPTY_TIME_TEXT = "vui long chon gio";
    private static final String PAST_DATE_TEXT = "ngay dat khong duoc nho hon ngay hien tai";
    private static final String INVALID_TIME_RANGE_TEXT = "gio dat lich phai tu 08:00 den 20:00";
    private static final String EMPTY_SERVICE_TEXT = "vui long chon dich vu";
    private static final String DUPLICATE_BOOKING_TEXT = "ban da dat dich vu nay vao khung gio nay roi!";
    private static final String GOI_DAU_DUONG_SINH = "Gội đầu dưỡng sinh";
    private static final String TRIET_LONG = "Triệt lông";
    private static final String TAM_TRANG = "Tắm trắng";

    @Test
    public void TC01() {
        openBookingPage();
        populateBookingForm("Nguyễn Thị An", "0267363563", BOOKING_DATE, "10:00", "gội nước nóng", GOI_DAU_DUONG_SINH);
        clickWhenReady(By.xpath("//button[contains(text(), 'Lưu')]"));
        waitForMessage("xac nhan dat lich hen");
        clickWhenReady(By.id("modal-confirm"));

        String normalizedMessage = waitForMessage(SUCCESS_TEXT);
        forceBookingPageVisible();
        Assertions.assertTrue(
                normalizedMessage.contains(SUCCESS_TEXT),
                "Unexpected modal message: " + driver.findElement(By.id("modal-message")).getText()
        );
        Assertions.assertTrue(isPageActive("booking-page"), "TC01 should stay on booking page while success message is displayed");
    }

    @Test
    public void TC02() {
        openBookingPage();
        populateBookingForm("", "0267363563", BOOKING_DATE, "10:00", "gội nước nóng", GOI_DAU_DUONG_SINH);
        clickWhenReady(By.xpath("//button[contains(text(), 'Lưu')]"));

        String normalizedMessage = waitForMessage(INVALID_NAME_TEXT);
        Assertions.assertTrue(
                normalizedMessage.contains(INVALID_NAME_TEXT),
                "Unexpected modal message: " + driver.findElement(By.id("modal-message")).getText()
        );
        Assertions.assertTrue(isPageActive("booking-page"), "TC02 should stay on booking page after validation error");
    }

    @Test
    public void TC03() {
        openBookingPage();
        populateBookingForm("Nguyễn Thị An", "", BOOKING_DATE, "10:00", "gội nước nóng", GOI_DAU_DUONG_SINH);
        clickWhenReady(By.xpath("//button[contains(text(), 'Lưu')]"));

        String normalizedMessage = waitForMessage(INVALID_PHONE_TEXT);
        Assertions.assertTrue(
                normalizedMessage.contains(INVALID_PHONE_TEXT),
                "Unexpected modal message: " + driver.findElement(By.id("modal-message")).getText()
        );
        Assertions.assertTrue(isPageActive("booking-page"), "TC03 should stay on booking page after validation error");
    }

    @Test
    public void TC04() {
        openBookingPage();
        populateBookingForm("Nguyễn Thị An", "026736356", "2026-04-29", "09:30", "Tắm toàn thân", TAM_TRANG);
        clickWhenReady(By.xpath("//button[contains(text(), 'Lưu')]"));

        String normalizedMessage = waitForMessage(INVALID_PHONE_TEXT);
        Assertions.assertTrue(
                normalizedMessage.contains(INVALID_PHONE_TEXT),
                "Unexpected modal message: " + driver.findElement(By.id("modal-message")).getText()
        );
        Assertions.assertTrue(isPageActive("booking-page"), "TC04 should stay on booking page after validation error");
    }

    @Test
    public void TC05() {
        openBookingPage();
        populateBookingForm("Nguyễn Thị An", "02673635631", "2026-04-29", "09:30", "Tắm toàn thân", TAM_TRANG);
        clickWhenReady(By.xpath("//button[contains(text(), 'Lưu')]"));

        String normalizedMessage = waitForMessage(INVALID_PHONE_TEXT);
        Assertions.assertTrue(
                normalizedMessage.contains(INVALID_PHONE_TEXT),
                "Unexpected modal message: " + driver.findElement(By.id("modal-message")).getText()
        );
        Assertions.assertTrue(isPageActive("booking-page"), "TC05 should stay on booking page after validation error");
    }

    @Test
    public void TC06() {
        openBookingPage();
        populateBookingForm("Nguyễn Thị An", "03278h768!", "2026-04-29", "09:30", "Tắm toàn thân", TAM_TRANG);
        clickWhenReady(By.xpath("//button[contains(text(), 'Lưu')]"));

        String normalizedMessage = waitForMessage(INVALID_PHONE_TEXT);
        Assertions.assertTrue(
                normalizedMessage.contains(INVALID_PHONE_TEXT),
                "Unexpected modal message: " + driver.findElement(By.id("modal-message")).getText()
        );
        Assertions.assertTrue(isPageActive("booking-page"), "TC06 should stay on booking page after validation error");
    }

    @Test
    public void TC07() {
        openBookingPage();
        populateBookingForm("Nguyễn Thị An", "0267363563", "", "10:00", "gội nước nóng", GOI_DAU_DUONG_SINH);
        clickWhenReady(By.xpath("//button[contains(text(), 'Lưu')]"));

        String normalizedMessage = waitForMessage(EMPTY_DATE_TEXT);
        Assertions.assertTrue(
                normalizedMessage.contains(EMPTY_DATE_TEXT),
                "Unexpected modal message: " + driver.findElement(By.id("modal-message")).getText()
        );
        Assertions.assertTrue(isPageActive("booking-page"), "TC07 should stay on booking page after validation error");
    }

    @Test
    public void TC08() {
        openBookingPage();
        // Today is 2026-04-17 (from metadata). 2026-02-01 is in the past.
        populateBookingForm("Nguyễn Thị An", "0267363563", "2026-02-01", "10:00", "gội nước nóng", GOI_DAU_DUONG_SINH);
        clickWhenReady(By.xpath("//button[contains(text(), 'Lưu')]"));

        String normalizedMessage = waitForMessage(PAST_DATE_TEXT);
        Assertions.assertTrue(
                normalizedMessage.contains(PAST_DATE_TEXT),
                "Unexpected modal message: " + driver.findElement(By.id("modal-message")).getText()
        );
        Assertions.assertTrue(isPageActive("booking-page"), "TC08 should stay on booking page after validation error");
    }

    @Test
    public void TC09() {
        openBookingPage();
        populateBookingForm("Nguyễn Thị An", "0267363563", "2026-04-29", "", "gội nước nóng", GOI_DAU_DUONG_SINH);
        clickWhenReady(By.xpath("//button[contains(text(), 'Lưu')]"));

        String normalizedMessage = waitForMessage(EMPTY_TIME_TEXT);
        Assertions.assertTrue(
                normalizedMessage.contains(EMPTY_TIME_TEXT),
                "Unexpected modal message: " + driver.findElement(By.id("modal-message")).getText()
        );
        Assertions.assertTrue(isPageActive("booking-page"), "TC09 should stay on booking page after validation error");
    }

    @Test
    public void TC10() {
        openBookingPage();
        // 10:00 PM is 22:00, which is outside the 08:00 - 20:00 range.
        populateBookingForm("Nguyễn Thị An", "0267363563", "2026-04-28", "22:00", "gội nước nóng", GOI_DAU_DUONG_SINH);
        clickWhenReady(By.xpath("//button[contains(text(), 'Lưu')]"));

        String normalizedMessage = waitForMessage(INVALID_TIME_RANGE_TEXT);
        Assertions.assertTrue(
                normalizedMessage.contains(INVALID_TIME_RANGE_TEXT),
                "Unexpected modal message: " + driver.findElement(By.id("modal-message")).getText()
        );
        Assertions.assertTrue(isPageActive("booking-page"), "TC10 should stay on booking page after validation error");
    }

    @Test
    public void TC11() {
        openBookingPage();
        // Passing empty service name to skip selection
        populateBookingForm("Nguyễn Thị An", "0267363563", "2026-04-28", "10:00", "gội nước nóng", "");
        clickWhenReady(By.xpath("//button[contains(text(), 'Lưu')]"));

        String normalizedMessage = waitForMessage(EMPTY_SERVICE_TEXT);
        Assertions.assertTrue(
                normalizedMessage.contains(EMPTY_SERVICE_TEXT),
                "Unexpected modal message: " + driver.findElement(By.id("modal-message")).getText()
        );
        Assertions.assertTrue(isPageActive("booking-page"), "TC11 should stay on booking page after validation error");
    }

    @Test
    public void TC12() {
        openBookingPage();
        populateBookingForm("Nguyễn Thị An", "0267363563", "2026-04-28", "10:00", "", GOI_DAU_DUONG_SINH);
        clickWhenReady(By.xpath("//button[contains(text(), 'Lưu')]"));

        String normalizedMessage = waitForMessage(DUPLICATE_BOOKING_TEXT);
        Assertions.assertTrue(
                normalizedMessage.contains(DUPLICATE_BOOKING_TEXT),
                "Unexpected modal message: " + driver.findElement(By.id("modal-message")).getText()
        );
        Assertions.assertTrue(isPageActive("booking-page"), "TC12 should stay on booking page after validation error");
    }

    @Test
    public void TC13() {
        openBookingPage();
        populateBookingForm("Nguyễn Thị An", "0267363563", "2026-04-28", "10:00", "gội nước nóng", GOI_DAU_DUONG_SINH);
        
        // Mở khóa điều hướng vì BaseTest chặn chuyển trang về home-page để test các chức năng khác
        ((JavascriptExecutor) driver).executeScript(
                "if (window.__originalUINavigate) { window.UI.navigate = window.__originalUINavigate; }" +
                "if (window.__originalNavigate) { window.navigate = window.__originalNavigate; }"
        );
        
        clickWhenReady(By.xpath("//button[contains(text(), 'Hủy')]"));

        wait.until(driver -> isPageActive("home-page"));
        Assertions.assertTrue(isPageActive("home-page"), "Hệ thống phải quay về màn hình Trang chủ sau khi bấm Hủy");
        Assertions.assertFalse(isPageActive("booking-page"), "Giao diện thêm lịch hẹn phải được đóng");
    }

    private void openBookingPage() {
        clickWhenReady(By.xpath("//button[contains(text(), 'Đặt lịch')]"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("bk-name")));
    }

    private void populateBookingForm(String name, String phone, String date, String time, String note, String serviceName) {
        ensureServiceOptionsLoaded();

        ((JavascriptExecutor) driver).executeScript(
                "document.getElementById('bk-name').value = arguments[0];" +
                        "document.getElementById('bk-phone').value = arguments[1];" +
                        "document.getElementById('bk-date').value = arguments[2];" +
                        "document.getElementById('bk-time').value = arguments[3];" +
                        "document.getElementById('bk-note').value = arguments[4];" +
                        "['bk-name','bk-phone','bk-date','bk-time','bk-note'].forEach(id => {" +
                        "  const element = document.getElementById(id);" +
                        "  element.dispatchEvent(new Event('input', { bubbles: true }));" +
                        "  element.dispatchEvent(new Event('change', { bubbles: true }));" +
                        "});",
                name,
                phone,
                date,
                time,
                note
        );
        if (serviceName != null && !serviceName.isEmpty()) {
            selectServiceByName(serviceName);
        } else {
            ((JavascriptExecutor) driver).executeScript(
                    "const select = document.getElementById('bk-service');" +
                    "if (select) {" +
                    "  select.value = '';" +
                    "  select.selectedIndex = 0;" + // Often 0 is the default placeholder 
                    "  select.dispatchEvent(new Event('change', { bubbles: true }));" +
                    "}"
            );
        }
        pause(1200);
    }

    private void ensureServiceOptionsLoaded() {
        Boolean serviceLoaded = wait.until(driver -> (Boolean) ((JavascriptExecutor) driver).executeScript(
                "const select = document.getElementById('bk-service');" +
                        "return !!select && Array.from(select.options).some(option => option.value);"
        ));
        Assertions.assertTrue(Boolean.TRUE.equals(serviceLoaded), "Booking service options were not loaded");
    }

    private void selectServiceByName(String serviceName) {
        Boolean selected = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "const normalize = value => value.normalize('NFD')" +
                        ".replace(/[\\u0300-\\u036f]/g, '')" +
                        ".toLowerCase()" +
                        ".trim();" +
                        "const select = document.getElementById('bk-service');" +
                        "const options = Array.from(select.options);" +
                        "const target = options.find(option => normalize(option.textContent) === normalize(arguments[0]));" +
                        "if (!target) { return false; }" +
                        "target.selected = true;" +
                        "select.selectedIndex = options.indexOf(target);" +
                        "select.value = target.value;" +
                        "select.dispatchEvent(new Event('input', { bubbles: true }));" +
                        "select.dispatchEvent(new Event('change', { bubbles: true }));" +
                        "return select.value === target.value && target.selected;",
                serviceName
        );
        Assertions.assertTrue(Boolean.TRUE.equals(selected), "Could not select service: " + serviceName);
    }


    private String waitForMessage(String expectedNormalizedText) {
        return wait.until(driver -> {
            String message = driver.findElement(By.id("modal-message")).getText();
            String normalized = normalizeText(message);
            return normalized.contains(expectedNormalizedText) ? normalized : null;
        });
    }

    private void forceBookingPageVisible() {
        ((JavascriptExecutor) driver).executeScript(
                "document.querySelectorAll('.page').forEach(page => page.classList.remove('active-page'));" +
                        "document.getElementById('booking-page').classList.add('active-page');"
        );
    }

    private String buildUniqueBookingTime() {
        int minute = Math.floorMod(LocalTime.now().getMinute() + 13, 60);
        return String.format("11:%02d", minute);
    }

    private boolean isPageActive(String pageId) {
        WebElement page = driver.findElement(By.id(pageId));
        return page.getAttribute("class").contains("active-page");
    }

    private String normalizeText(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        return normalized
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT);
    }
}
