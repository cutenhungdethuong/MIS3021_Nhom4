import org.testng.Assert;
import org.testng.annotations.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DeleteBookingTest extends BaseTest {
    private static final String SERVICE_TAM_TRANG = "DV003";
    private static final String SERVICE_CHAM_SOC_DA = "DV002";
    private static final String SERVICE_GOI_DAU = "DV004";
    private static final String SERVICE_DIEU_TRI_DA = "DV001";

    @Test
    public void TC01() {
        Map<String, Object> booking = createBooking("Chưa xác nhận", "2026-05-09", "09:30", SERVICE_TAM_TRANG,
                "delete-success");

        try {
            openViewBookingPage();
            openBookingDetail(booking);
            clickElement(driver.findElement(By.id("btn-delete")));

            WebElement confirmOverlay = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[style*='z-index: 3000']")));
            Assert.assertTrue(normalizeText(confirmOverlay.getText()).contains("ban co chac chan muon xoa"),
                    "Delete confirmation dialog should be displayed");

            clickElement(confirmOverlay.findElement(By.cssSelector(".btn-confirm")));

            WebElement successOverlay = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[style*='z-index: 3000']")));
            Assert.assertTrue(normalizeText(successOverlay.getText()).contains("da xoa thanh cong"),
                    "Success message should be displayed after deletion");

            clickElement(successOverlay.findElement(By.tagName("button")));

            Assert.assertFalse(isRowPresent(booking), "Deleted booking should not remain in the list");
        } finally {
            deleteBookingById(String.valueOf(booking.get("MaLichHen")));
        }
    }

    @Test
    public void TC02() {
        Map<String, Object> booking = createBooking("Chưa xác nhận", "2026-05-24", "19:20", SERVICE_CHAM_SOC_DA,
                "delete-cancel");

        try {
            openViewBookingPage();
            openBookingDetail(booking);
            clickElement(driver.findElement(By.id("btn-delete")));

            WebElement confirmOverlay = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[style*='z-index: 3000']")));
            Assert.assertTrue(normalizeText(confirmOverlay.getText()).contains("ban co chac chan muon xoa"),
                    "Delete confirmation dialog should be displayed");

            clickElement(confirmOverlay.findElement(By.cssSelector(".btn-cancel")));

            wait.until(ExpectedConditions.invisibilityOf(confirmOverlay));
            Assert.assertTrue(bookingExists(String.valueOf(booking.get("MaLichHen"))),
                    "Booking should remain in database after cancel");
            Assert.assertTrue(isRowPresent(booking), "Booking should remain visible in the list after cancel");
        } finally {
            deleteBookingById(String.valueOf(booking.get("MaLichHen")));
        }
    }

    @Test
    public void TC03() {
        Map<String, Object> booking = createBooking("Đã xác nhận", "2026-05-10", "19:20", SERVICE_GOI_DAU,
                "confirmed-booking");

        try {
            openViewBookingPage();
            openBookingDetail(booking);
            Assert.assertTrue(driver.findElements(By.id("btn-delete")).isEmpty(),
                    "Delete button must not be shown for confirmed bookings");
        } finally {
            deleteBookingById(String.valueOf(booking.get("MaLichHen")));
        }
    }

    @Test
    public void TC04() {
        Map<String, Object> booking = createBooking("Từ chối", "2026-05-12", "17:20", SERVICE_DIEU_TRI_DA,
                "rejected-booking");

        try {
            openViewBookingPage();
            openBookingDetail(booking);
            Assert.assertTrue(driver.findElements(By.id("btn-delete")).isEmpty(),
                    "Delete button must not be shown for rejected bookings");
        } finally {
            deleteBookingById(String.valueOf(booking.get("MaLichHen")));
        }
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
    }

    private void openBookingDetail(Map<String, Object> booking) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#list-page table")));
        WebElement row = wait.until(driver -> findBookingRow(booking));
        WebElement detailButton = row.findElements(By.tagName("td")).get(4).findElement(By.tagName("button"));
        clickElement(detailButton);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("edit-name")));
    }

    private WebElement findBookingRow(Map<String, Object> booking) {
        String expectedService = normalizeText(String.valueOf(booking.get("TenDV"))).trim();
        String expectedDate = formatDateForTable(String.valueOf(booking.get("NgayDatLich")));
        String expectedStatus = normalizeText(String.valueOf(booking.get("TrangThai"))).trim();

        List<WebElement> rows = driver.findElements(By.cssSelector("#booking-table-body tr"));
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (cells.size() < 5) {
                continue;
            }

            String service = normalizeText(cells.get(1).getText()).trim();
            String date = cells.get(2).getText().trim();
            String status = normalizeText(cells.get(3).getText()).trim();

            if (service.equals(expectedService) && date.equals(expectedDate) && status.equals(expectedStatus)) {
                return row;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> createBooking(String status, String date, String time, String serviceId, String note) {
        Object result = ((JavascriptExecutor) driver).executeAsyncScript(
                "const done = arguments[arguments.length - 1];" +
                        "(async () => {" +
                        "  try {" +
                        "    const currentUser = window.Auth && window.Auth.currentUser;" +
                        "    const serviceId = arguments[3];" +
                        "    const services = await window.DB.getAll('DichVu');" +
                        "    const service = services.find(s => s.MaDV === serviceId);" +
                        "    const payload = {" +
                        "      MaKH: currentUser.MaKH," +
                        "      MaDV: serviceId," +
                        "      MaNV: 'NV001'," +
                        "      TenKhach: currentUser.HoTen || 'Khach Hang'," +
                        "      SDTKhach: currentUser.SDT || '0932596352'," +
                        "      NgayDatLich: arguments[1]," +
                        "      GioDatLich: arguments[2]," +
                        "      TrangThai: arguments[0]," +
                        "      GhiChu: arguments[4]" +
                        "    };" +
                        "    const inserted = await window.DB.insert('LichHen', payload);" +
                        "    done({" +
                        "      MaLichHen: inserted.MaLichHen," +
                        "      NgayDatLich: inserted.NgayDatLich," +
                        "      GioDatLich: inserted.GioDatLich," +
                        "      TrangThai: inserted.TrangThai," +
                        "      TenDV: service ? service.TenDV : ''," +
                        "      MaDV: inserted.MaDV" +
                        "    });" +
                        "  } catch (error) {" +
                        "    done({});" +
                        "  }" +
                        "})();",
                status,
                date,
                time,
                serviceId,
                note);
        Map<String, Object> booking = (Map<String, Object>) result;
        Assert.assertFalse(booking.isEmpty(), "Could not create booking test data");
        return booking;
    }

    private boolean bookingExists(String bookingId) {
        Object result = ((JavascriptExecutor) driver).executeAsyncScript(
                "const done = arguments[arguments.length - 1];" +
                        "window.DB.findOne('LichHen', { MaLichHen: arguments[0] })" +
                        "  .then(booking => done(!!booking))" +
                        "  .catch(() => done(false));",
                bookingId);
        return Boolean.TRUE.equals(result);
    }

    private void deleteBookingById(String bookingId) {
        ((JavascriptExecutor) driver).executeAsyncScript(
                "const done = arguments[arguments.length - 1];" +
                        "window.DB.delete('LichHen', arguments[0])" +
                        "  .then(() => done(true))" +
                        "  .catch(() => done(false));",
                bookingId);
    }

    private boolean isRowPresent(Map<String, Object> booking) {
        try {
            ((JavascriptExecutor) driver).executeAsyncScript(
                    "const done = arguments[arguments.length - 1];" +
                            "if (window.Booking && typeof window.Booking.renderList === 'function') {" +
                            "  window.Booking.renderList().then(() => done(true)).catch(() => done(true));" +
                            "} else {" +
                            "  done(true);" +
                            "}");
        } catch (Exception ignored) {
        }

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("booking-table-body")));
        } catch (TimeoutException ignored) {
            return false;
        }
        return findBookingRow(booking) != null;
    }

    private void clickElement(WebElement element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        } catch (Exception ignored) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
        pause(1200);
    }

    private boolean isPageActive(String pageId) {
        WebElement page = driver.findElement(By.id(pageId));
        return page.getAttribute("class").contains("active-page");
    }

    private String formatDateForTable(String dateValue) {
        String[] parts = dateValue.split("-");
        return parts[2] + "/" + parts[1] + "/" + parts[0];
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