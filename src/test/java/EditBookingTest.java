import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditBookingTest extends BaseTest {
    private static final String SERVICE_DIEU_TRI_DA = "DV001";
    private static final String SERVICE_CHAM_SOC_DA = "DV002";
    private static final String SERVICE_TAM_TRANG = "DV003";
    private static final String SERVICE_GOI_DAU = "DV004";

    @Test
    public void TC01() {
        Map<String, Object> booking = createBooking("Đã xác nhận", "2026-04-01", "09:00", SERVICE_DIEU_TRI_DA,
                "Khách muốn làm kỹ vùng chữ T");

        try {
            openViewBookingPage();
            openBookingDetail(booking);
            Assert.assertTrue(driver.findElements(By.id("btn-edit")).isEmpty(),
                    "Edit button must not be shown for confirmed bookings");
            Assert.assertTrue(driver.findElements(By.id("btn-delete")).isEmpty(),
                    "Delete button must not be shown for confirmed bookings");
        } finally {
            deleteBookingById(String.valueOf(booking.get("MaLichHen")));
        }
    }

    @Test
    public void TC02() {
        Map<String, Object> booking = createBooking("Chưa xác nhận", "2026-05-28", "10:00", SERVICE_GOI_DAU,
                "goi nuoc nong");

        try {
            openViewBookingPage();
            openBookingDetail(booking);
            enterEditMode();

            setEditFormValues(
                    currentUserName(),
                    currentUserPhone(),
                    "2026-05-28",
                    "10:00",
                    SERVICE_GOI_DAU,
                    "Tam toan than hop le");

            clickSaveButton();
            assertOverlayContains("cap nhat thanh cong!");

            Map<String, Object> updated = wait.until(driver -> {
                Map<String, Object> refreshed = getBookingById(String.valueOf(booking.get("MaLichHen")));
                return "Tam toan than hop le".equals(String.valueOf(refreshed.get("GhiChu"))) ? refreshed : null;
            });
            Assert.assertEquals("Tam toan than hop le", String.valueOf(updated.get("GhiChu")));
        } finally {
            deleteBookingById(String.valueOf(booking.get("MaLichHen")));
        }
    }

    @Test
    public void TC03() {
        Map<String, Object> booking = createBooking("Chưa xác nhận", "2026-05-28", "10:00", SERVICE_TAM_TRANG,
                "goi nuoc nong");

        try {
            openViewBookingPage();
            openBookingDetail(booking);
            enterEditMode();

            setEditFormValues(
                    currentUserName(),
                    currentUserPhone(),
                    "2026-02-01",
                    "10:00",
                    SERVICE_TAM_TRANG,
                    "goi nuoc nong hop le");

            clickSaveButton();
            assertOverlayContains("ngay dat khong duoc nho hon ngay hien tai");
        } finally {
            deleteBookingById(String.valueOf(booking.get("MaLichHen")));
        }
    }

    @Test
    public void TC04() {
        Map<String, Object> booking = createBooking("Chưa xác nhận", "2026-05-29", "08:30", SERVICE_GOI_DAU,
                "goi nuoc nong hop le");

        try {
            openViewBookingPage();
            openBookingDetail(booking);
            enterEditMode();

            setEditFormValues(
                    currentUserName(),
                    currentUserPhone(),
                    "2026-05-29",
                    "07:30",
                    SERVICE_GOI_DAU,
                    "goi nuoc nong hop le");

            clickSaveButton();
            assertOverlayContains("gio dat lich phai tu 08:00 den 20:00");
        } finally {
            deleteBookingById(String.valueOf(booking.get("MaLichHen")));
        }
    }

    @Test
    public void TC05() {
        Map<String, Object> targetBooking = createBooking("Chưa xác nhận", "2026-05-28", "10:00", SERVICE_GOI_DAU,
                "target-booking");
        Map<String, Object> editableBooking = createBooking("Chưa xác nhận", "2026-05-29", "11:30", SERVICE_CHAM_SOC_DA,
                "editable-booking");

        try {
            openViewBookingPage();
            openBookingDetail(editableBooking);
            enterEditMode();

            setEditFormValues(
                    currentUserName(),
                    currentUserPhone(),
                    "2026-05-28",
                    "10:00",
                    SERVICE_GOI_DAU,
                    "duplicate-booking");

            clickSaveButton();
            assertOverlayContains("ban da dat dich vu nay vao khung gio nay roi");
        } finally {
            deleteBookingById(String.valueOf(targetBooking.get("MaLichHen")));
            deleteBookingById(String.valueOf(editableBooking.get("MaLichHen")));
        }
    }

    @Test
    public void TC06() {
        Map<String, Object> booking = createBooking("Chưa xác nhận", "2026-05-29", "09:30", SERVICE_TAM_TRANG,
                "Tam toan than hop le");

        try {
            openViewBookingPage();
            openBookingDetail(booking);
            enterEditMode();

            WebElement cancelButton = driver.findElement(By.id("btn-delete"));
            Assert.assertEquals("huy", normalizeText(cancelButton.getText()).trim());
            clickElement(cancelButton);

            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("edit-name")));
            Assert.assertTrue(isPageActive("list-page"),
                    "Booking list page should remain active after canceling edit");
        } finally {
            deleteBookingById(String.valueOf(booking.get("MaLichHen")));
        }
    }

    @Test
    public void TC07() {
        Map<String, Object> booking = createBooking("Chưa xác nhận", "2026-05-29", "09:30", SERVICE_TAM_TRANG,
                "Tam toan than hop le");

        try {
            openViewBookingPage();
            openBookingDetail(booking);
            enterEditMode();

            setEditFormValues(
                    currentUserName(),
                    "026736356",
                    "2026-05-29",
                    "09:30",
                    SERVICE_TAM_TRANG,
                    "Tam toan than hop le");

            clickSaveButton();
            assertOverlayContains("so dien thoai khong hop le");
        } finally {
            deleteBookingById(String.valueOf(booking.get("MaLichHen")));
        }
    }

    @Test
    public void TC08() {
        Map<String, Object> booking = createBooking("Chưa xác nhận", "2026-05-29", "09:30", SERVICE_TAM_TRANG,
                "Tam toan than hop le");

        try {
            openViewBookingPage();
            openBookingDetail(booking);
            enterEditMode();

            setEditFormValues(
                    currentUserName(),
                    "02673635631",
                    "2026-05-29",
                    "09:30",
                    SERVICE_TAM_TRANG,
                    "Tam toan than hop le");

            clickSaveButton();
            assertOverlayContains("so dien thoai khong hop le");
        } finally {
            deleteBookingById(String.valueOf(booking.get("MaLichHen")));
        }
    }

    // @Disabled("Current app code does not implement an upper bound check for
    // far-future booking dates")

    // @Test
    // public void TC09() {
    // Map<String, Object> booking = createBooking("Chưa xác nhận", "2026-05-29",
    // "09:30", SERVICE_TAM_TRANG, "Tam toan than hop le");
    //
    //
    // try {
    // openViewBookingPage();
    // openBookingDetail(booking);
    // enterEditMode();
    //
    //
    // setEditFormValues(
    // currentUserName(),
    // currentUserPhone(),
    // "2026-05-29",
    // "08:00",
    // SERVICE_TAM_TRANG,
    // "Mo cua"
    // );
    //
    //
    // clickSaveButton();
    // assertOverlayContains("cap nhat thanh cong");
    // } finally {
    // deleteBookingById(String.valueOf(booking.get("MaLichHen")));
    // }
    // }

    //
    // @Test
    // public void TC10() {
    // Map<String, Object> booking = createBooking("Chưa xác nhận", "2026-05-29",
    // "09:30", SERVICE_TAM_TRANG, "Tam toan than hop le");
    //
    //
    // try {
    // openViewBookingPage();
    // openBookingDetail(booking);
    // enterEditMode();
    //
    //
    // setEditFormValues(
    // currentUserName(),
    // currentUserPhone(),
    // "2026-05-29",
    // "20:00",
    // SERVICE_TAM_TRANG,
    // "Dong cua"
    // );
    //
    //
    // clickSaveButton();
    // assertOverlayContains("cap nhat thanh cong");
    // } finally {
    // deleteBookingById(String.valueOf(booking.get("MaLichHen")));
    // }
    // }

    @Test
    public void TC09() {
        Map<String, Object> booking = createBooking("Chưa xác nhận", "2026-05-29", "09:30", SERVICE_TAM_TRANG,
                "Tam toan than hop le");

        try {
            openViewBookingPage();
            openBookingDetail(booking);
            enterEditMode();

            setEditFormValues(
                    currentUserName(),
                    currentUserPhone(),
                    "2026-05-29",
                    "07:59",
                    SERVICE_TAM_TRANG,
                    "Truoc gio mo cua");

            clickSaveButton();
            assertOverlayContains("gio dat lich phai tu 08:00 den 20:00");
        } finally {
            deleteBookingById(String.valueOf(booking.get("MaLichHen")));
        }
    }

    @Test
    public void TC10() {
        Map<String, Object> booking = createBooking("Chưa xác nhận", "2026-05-29", "09:30", SERVICE_TAM_TRANG,
                "Tam toan than hop le");

        try {
            openViewBookingPage();
            openBookingDetail(booking);
            enterEditMode();

            setEditFormValues(
                    currentUserName(),
                    currentUserPhone(),
                    "2026-05-29",
                    "20:01",
                    SERVICE_TAM_TRANG,
                    "Sau gio dong cua");

            clickSaveButton();
            assertOverlayContains("gio dat lich phai tu 08:00 den 20:00");
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

    private void enterEditMode() {
        clickElement(driver.findElement(By.id("btn-edit")));
        wait.until(driver -> normalizeText(driver.findElement(By.id("btn-edit")).getText()).trim().equals("luu"));
    }

    private void clickSaveButton() {
        clickElement(driver.findElement(By.id("btn-edit")));
    }

    private WebElement findBookingRow(Map<String, Object> booking) {
        String expectedService = normalizeText(String.valueOf(booking.get("TenDV"))).trim();
        String expectedDate = formatDateForTable(String.valueOf(booking.get("NgayDatLich")));
        String expectedStatus = normalizeStatus(String.valueOf(booking.get("TrangThai")));

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
                        "    const services = await window.DB.getAll('DichVu');" +
                        "    const service = services.find(s => s.MaDV === arguments[3]);" +
                        "    const inserted = await window.DB.insert('LichHen', {" +
                        "      MaKH: currentUser.MaKH," +
                        "      MaDV: arguments[3]," +
                        "      MaNV: 'NV001'," +
                        "      TenKhach: currentUser.HoTen || 'Khach Hang'," +
                        "      SDTKhach: currentUser.SDT || '0932596352'," +
                        "      NgayDatLich: arguments[1]," +
                        "      GioDatLich: arguments[2]," +
                        "      TrangThai: arguments[0]," +
                        "      GhiChu: arguments[4]" +
                        "    });" +
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> getBookingById(String bookingId) {
        Object result = ((JavascriptExecutor) driver).executeAsyncScript(
                "const done = arguments[arguments.length - 1];" +
                        "window.DB.findOne('LichHen', { MaLichHen: arguments[0] })" +
                        "  .then(booking => done(booking || {}))" +
                        "  .catch(() => done({}));",
                bookingId);
        return (Map<String, Object>) result;
    }

    private void deleteBookingById(String bookingId) {
        ((JavascriptExecutor) driver).executeAsyncScript(
                "const done = arguments[arguments.length - 1];" +
                        "window.DB.delete('LichHen', arguments[0])" +
                        "  .then(() => done(true))" +
                        "  .catch(() => done(false));",
                bookingId);
    }

    private void setEditFormValues(String name, String phone, String date, String time, String serviceId, String note) {
        ((JavascriptExecutor) driver).executeScript(
                "document.getElementById('edit-name').value = arguments[0];" +
                        "document.getElementById('edit-phone').value = arguments[1];" +
                        "document.getElementById('edit-date').value = arguments[2];" +
                        "document.getElementById('edit-time').value = arguments[3];" +
                        "document.getElementById('edit-note').value = arguments[5];" +
                        "const service = document.getElementById('edit-service');" +
                        "const options = Array.from(service.options);" +
                        "const target = options.find(option => option.value === arguments[4]);" +
                        "if (target) {" +
                        "  target.selected = true;" +
                        "  service.value = target.value;" +
                        "  service.selectedIndex = options.indexOf(target);" +
                        "}" +
                        "['edit-name','edit-phone','edit-date','edit-time','edit-note','edit-service'].forEach(id => {"
                        +
                        "  const element = document.getElementById(id);" +
                        "  element.dispatchEvent(new Event('input', { bubbles: true }));" +
                        "  element.dispatchEvent(new Event('change', { bubbles: true }));" +
                        "});",
                name,
                phone,
                date,
                time,
                serviceId,
                note);
        pause(1200);
    }

    private String currentUserName() {
        Object result = ((JavascriptExecutor) driver)
                .executeScript("return window.Auth && window.Auth.currentUser ? window.Auth.currentUser.HoTen : '';");
        return String.valueOf(result);
    }

    private String currentUserPhone() {
        Object result = ((JavascriptExecutor) driver)
                .executeScript("return window.Auth && window.Auth.currentUser ? window.Auth.currentUser.SDT : '';");
        return String.valueOf(result);
    }

    private void clickElement(WebElement element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        } catch (Exception ignored) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
        pause(1200);
    }

    private void assertOverlayContains(String expectedNormalizedText) {
        WebElement overlay = wait
                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[style*='z-index: 3000']")));
        String normalized = normalizeText(overlay.getText());
        Assert.assertTrue(normalized.contains(expectedNormalizedText),
                "Unexpected overlay text: " + overlay.getText());
    }

    private boolean isPageActive(String pageId) {
        WebElement page = driver.findElement(By.id(pageId));
        return page.getAttribute("class").contains("active-page");
    }

    private String normalizeStatus(String value) {
        String normalized = normalizeText(value).trim();
        if (normalized.equals("hoan thanh")) {
            return "da xac nhan";
        }
        if (normalized.equals("da huy")) {
            return "tu choi";
        }
        return normalized;
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
