import org.testng.Assert;
import org.testng.annotations.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ViewBookingTest extends BaseTest {
    private static final String EMPTY_BOOKING_USERNAME = "emptybooking";
    private static final String EMPTY_BOOKING_PASSWORD = "12345678";
    private static final String EMPTY_BOOKING_PHONE = "0900000001";
    private static final String EMPTY_BOOKING_NAME = "Khach Hang Rong";

    @Test
    public void TC01() {
        openViewBookingPage();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#list-page table")));

        List<WebElement> headers = driver.findElements(By.cssSelector("#list-page thead th"));
        Assert.assertEquals(5, headers.size(), "View booking table should contain 5 columns");
        Assert.assertEquals("stt", normalizeText(headers.get(0).getText()).trim());
        Assert.assertEquals("loai dich vu", normalizeText(headers.get(1).getText()).trim());
        Assert.assertEquals("ngay hen", normalizeText(headers.get(2).getText()).trim());
        Assert.assertEquals("trang thai", normalizeText(headers.get(3).getText()).trim());
        Assert.assertEquals("chi tiet", normalizeText(headers.get(4).getText()).trim());

        Long expectedBookingCount = ((Number) ((JavascriptExecutor) driver).executeAsyncScript(
                "const done = arguments[arguments.length - 1];" +
                        "window.DB.getAll('LichHen')" +
                        "  .then(bookings => {" +
                        "    const currentUser = window.Auth && window.Auth.currentUser;" +
                        "    const count = bookings.filter(b => currentUser && b.MaKH === currentUser.MaKH).length;" +
                        "    done(count);" +
                        "  })" +
                        "  .catch(() => done(-1));"))
                .longValue();

        Assert.assertTrue(expectedBookingCount > 0,
                "Test data must contain at least one booking for the logged-in customer");

        wait.until(driver -> driver.findElements(By.cssSelector("#booking-table-body tr"))
                .size() == expectedBookingCount.intValue());

        List<WebElement> rows = driver.findElements(By.cssSelector("#booking-table-body tr"));
        Assert.assertEquals(expectedBookingCount.intValue(), rows.size(),
                "Displayed booking rows should match the logged-in customer's data");

        for (int i = 0; i < rows.size(); i++) {
            List<WebElement> cells = rows.get(i).findElements(By.tagName("td"));
            Assert.assertEquals(5, cells.size(), "Each booking row should contain 5 cells");
            Assert.assertEquals(String.valueOf(i + 1), cells.get(0).getText().trim(),
                    "STT should increase sequentially");
            Assert.assertFalse(cells.get(1).getText().trim().isEmpty(), "Service name should be displayed");
            Assert.assertTrue(cells.get(2).getText().trim().matches("\\d{2}/\\d{2}/\\d{4}"),
                    "Booking date should be displayed as dd/MM/yyyy");

            String normalizedStatus = normalizeText(cells.get(3).getText()).trim();
            Assert.assertTrue(
                    normalizedStatus.equals("da xac nhan")
                            || normalizedStatus.equals("chua xac nhan")
                            || normalizedStatus.equals("tu choi"),
                    "Unexpected booking status: " + cells.get(3).getText());

            WebElement detailButton = cells.get(4).findElement(By.tagName("button"));
            Assert.assertEquals("xem chi tiet", normalizeText(detailButton.getText()).trim());
            Assert.assertTrue(detailButton.isDisplayed(), "Detail button should be visible");
        }
    }

    @Test
    public void TC02() {
        openViewBookingPage();
        wait.until(driver -> !driver.findElements(By.cssSelector("#booking-table-body tr")).isEmpty());

        Map<String, Object> expectedBooking = getFirstBookingForCurrentUser();
        Assert.assertFalse(expectedBooking.isEmpty(),
                "Test data must contain at least one booking for detail view");

        WebElement firstRow = driver.findElements(By.cssSelector("#booking-table-body tr")).get(0);
        WebElement detailButton = firstRow.findElements(By.tagName("td")).get(4).findElement(By.tagName("button"));
        clickElement(detailButton);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("edit-name")));

        WebElement modalTitle = driver.findElement(By.cssSelector("#modal-message h2"));
        Assert.assertEquals("chi tiet lich hen", normalizeText(modalTitle.getText()).trim());
        Assert.assertEquals(String.valueOf(expectedBooking.get("TenKhach")),
                driver.findElement(By.id("edit-name")).getAttribute("value"));
        Assert.assertEquals(String.valueOf(expectedBooking.get("SDTKhach")),
                driver.findElement(By.id("edit-phone")).getAttribute("value"));
        Assert.assertEquals(normalizeDateForInput(String.valueOf(expectedBooking.get("NgayDatLich"))),
                driver.findElement(By.id("edit-date")).getAttribute("value"));
        Assert.assertEquals(String.valueOf(expectedBooking.get("GioDatLich")),
                driver.findElement(By.id("edit-time")).getAttribute("value"));
        Assert.assertEquals(normalizeNullValue(expectedBooking.get("GhiChu")),
                driver.findElement(By.id("edit-note")).getAttribute("value"));
        WebElement serviceSelect = driver.findElement(By.id("edit-service"));
        String selectedService = serviceSelect.findElement(By.cssSelector("option:checked")).getText();
        Assert.assertEquals(String.valueOf(expectedBooking.get("TenDV")), selectedService);

        Assert.assertTrue(driver.findElement(By.id("edit-name")).isDisplayed(),
                "Customer name should be shown in detail popup");
        Assert.assertTrue(driver.findElement(By.id("edit-phone")).isDisplayed(),
                "Customer phone should be shown in detail popup");
        Assert.assertTrue(driver.findElement(By.id("edit-date")).isDisplayed(),
                "Booking date should be shown in detail popup");
        Assert.assertTrue(driver.findElement(By.id("edit-time")).isDisplayed(),
                "Booking time should be shown in detail popup");
        Assert.assertTrue(driver.findElement(By.id("edit-service")).isDisplayed(),
                "Booking service should be shown in detail popup");
        Assert.assertTrue(driver.findElement(By.id("edit-note")).isDisplayed(),
                "Booking note should be shown in detail popup");
    }

    private void openViewBookingPage() {
        ((JavascriptExecutor) driver).executeScript(
                "if (window.__originalUINavigate) { window.UI.navigate = window.__originalUINavigate; }" +
                        "if (window.__originalNavigate) { window.navigate = window.__originalNavigate; }");
        clickWhenReady(By.cssSelector("#nav-user button[data-page='list-page']"));
        if (!isPageActive("list-page")) {
            ((JavascriptExecutor) driver).executeScript("window.navigate('list-page');");
        }
        wait.until(driver -> isPageActive("list-page"));
        Assert.assertTrue(isPageActive("list-page"),
                "View booking page should be active after clicking the booking list button");
    }

    private void relogin(String username, String password) {
        ((JavascriptExecutor) driver).executeScript("window.handleLogout();");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nav-guest")));
        clickWhenReady(By.cssSelector("#nav-guest button[data-page='login-page']"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        setFieldValue(By.id("username"), username);
        setFieldValue(By.id("pwd"), password);

        WebElement agree = driver.findElement(By.id("login-agree"));
        if (!agree.isSelected()) {
            clickElement(agree);
        }

        clickWhenReady(By.cssSelector("button[onclick='handleLogin()']"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nav-user")));
        closeModalOverlayIfPresent();
        freezeAutoNavigation();
    }

    private void ensureEmptyBookingCustomerExists() {
        Boolean ready = (Boolean) ((JavascriptExecutor) driver).executeAsyncScript(
                "const done = arguments[arguments.length - 1];" +
                        "(async () => {" +
                        "  try {" +
                        "    let account = await window.DB.findOne('TaiKhoan', { TenDangNhap: arguments[0] });" +
                        "    if (!account) {" +
                        "      account = await window.DB.insert('TaiKhoan', {" +
                        "        TenDangNhap: arguments[0]," +
                        "        MatKhau: arguments[1]," +
                        "        VaiTro: 'khách hàng'," +
                        "        TrangThai: 'Hoạt động'" +
                        "      });" +
                        "    }" +
                        "    let customer = await window.DB.findOne('KhachHang', { MaTK: account.MaTK });" +
                        "    if (!customer) {" +
                        "      await window.DB.insert('KhachHang', {" +
                        "        HoTen: arguments[2]," +
                        "        SDT: arguments[3]," +
                        "        MaTK: account.MaTK" +
                        "      });" +
                        "    }" +
                        "    done(true);" +
                        "  } catch (error) {" +
                        "    done(false);" +
                        "  }" +
                        "})();",
                EMPTY_BOOKING_USERNAME,
                EMPTY_BOOKING_PASSWORD,
                EMPTY_BOOKING_NAME,
                EMPTY_BOOKING_PHONE);
        Assert.assertTrue(Boolean.TRUE.equals(ready), "Could not prepare customer test data without bookings");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getFirstBookingForCurrentUser() {
        Object result = ((JavascriptExecutor) driver).executeAsyncScript(
                "const done = arguments[arguments.length - 1];" +
                        "(async () => {" +
                        "  try {" +
                        "    const currentUser = window.Auth && window.Auth.currentUser;" +
                        "    const bookings = await window.DB.getAll('LichHen');" +
                        "    const services = await window.DB.getAll('DichVu');" +
                        "    const booking = bookings.find(b => currentUser && b.MaKH === currentUser.MaKH);" +
                        "    if (!booking) {" +
                        "      done({});" +
                        "      return;" +
                        "    }" +
                        "    const service = services.find(s => s.MaDV === booking.MaDV);" +
                        "    done({" +
                        "      TenKhach: booking.TenKhach || ''," +
                        "      SDTKhach: booking.SDTKhach || ''," +
                        "      NgayDatLich: booking.NgayDatLich || ''," +
                        "      GioDatLich: booking.GioDatLich || ''," +
                        "      GhiChu: booking.GhiChu || ''," +
                        "      TenDV: service ? service.TenDV : ''" +
                        "    });" +
                        "  } catch (error) {" +
                        "    done({});" +
                        "  }" +
                        "})();");
        return (Map<String, Object>) result;
    }

    private void clickElement(WebElement element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        } catch (Exception ignored) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
        pause(1200);
    }

    private String normalizeDateForInput(String dateValue) {
        if (dateValue != null && dateValue.contains("/")) {
            String[] parts = dateValue.split("/");
            return parts[2] + "-" + parts[1] + "-" + parts[0];
        }
        return dateValue == null ? "" : dateValue;
    }

    private String normalizeNullValue(Object value) {
        return value == null ? "" : String.valueOf(value);
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
