package ibs.test.practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class AddVegetableTest {

    private static final Logger logger = LoggerFactory.getLogger(AddVegetableTest.class);

    WebDriver driver;

    private static final String[] TEST_PRODUCTS = {"Огурец", "Батат"};
    private static final String URL = "http://localhost:8080";

    @BeforeEach
    public void setUp() {

        // Создаем папку "Working Project" на диске C
        Path workingDir = Path.of("C:/Working Project");
        try {
            if (!Files.exists(workingDir)) {
                Files.createDirectories(workingDir);
                logger.info("Создана папка: C:\\Working Project");
            }

            // Копируем .jar файл в папку "Working Project"
            Path jarFile = Path.of("src/test/resources/qualit-sandbox.jar");
            Path targetJar = workingDir.resolve("qualit-sandbox.jar");
            Files.copy(jarFile, targetJar, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Файл qualit-sandbox.jar скопирован в C:\\Working Project");

            //Путь к директории, в которой должен открыться PowerShell
            String path = "C:/Working Project";

            //Команда для запуска PowerShell с выполнением java -jar qualit-sandbox.jar
            String command = "cmd.exe /c start powershell.exe -NoExit -Command \"Set-Location '"
                    + path + "'; java -jar qualit-sandbox.jar\"";

            //Запуск процесса
            Process process = Runtime.getRuntime().exec(command);

            //Ожидание завершения процесса
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            logger.error("Ошибка в предусловии: {}", e.getMessage());
        }
    }

    @AfterEach
    public void clear() {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(10));

        try {

            // Открываем меню "Песочница"
            WebElement sandboxMenu = driver.findElement(By.xpath("//li[@class='nav-item dropdown']"));
            sandboxMenu.click();

            // Нажимаем кнопку "Сброс данных"
            WebElement sandboxMenuClear = driver.findElement(By.xpath("//a[@id='reset']"));
            sandboxMenuClear.click();

            // Проверяем, что удаленные товары отсутствуют в списке товаров
            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                    By.xpath("//*[contains(text(),'Огурец')]")));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                    By.xpath("//*[contains(text(),'Батат')]")));

            //Путь к директории, в которой должен открыться PowerShell
            String path = "C:/Working Project";

            //Команда для завершения работы стенда
            String command = "powershell.exe -NoExit -Command \"Set-Location '"
                    + path + "'; Stop-Process -Name java; exit\"";

            //Завершение работы стенда
            Process process = Runtime.getRuntime().exec(command);

            logger.info("Сброс данных завершен.");
        } catch (Exception e) {
            logger.error("Ошибка во время сброса данных: {}", e.getMessage());
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    @Test
    public void AddVegetable() throws InterruptedException {

        Thread.sleep(10000);
        System.setProperty("webdriver.chrome.driver", "src/test/resources/chromedriver.exe");

        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get(URL);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        logger.info("Тестирование стенда QualIT: {}", URL);

        // Открываем меню "Песочница"
        WebElement sandButton = driver.findElement(By.xpath("//li[@class='nav-item dropdown']"));
        sandButton.click();

        // Открываем страницу "Товары"
        WebElement productButton = driver.findElement(By.xpath("//a[@href='/food']"));
        productButton.click();

        // Ожидаем загрузки страницы товаров
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//button[@data-target='#editModal']")));

        // Проверяем, что добавляемые товары отсутствуют в списке товаров
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//*[contains(text(),'Огурец')]")));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//*[contains(text(),'Батат')]")));

        for (String productName : TEST_PRODUCTS) {
            logger.info("Тестирование продукта: {}", productName);

            // Добавляем товар
            addProduct(productName, "Овощ", productName.equals("Батат"));

            // Проверяем, что товар добавлен
            WebElement productRow = driver.findElement(
                    By.xpath("//*[contains(text(),'" + productName + "')]"));
            assertTrue(productRow.isDisplayed(),
                    "Продукт " + productName + " должен быть отображен в списке товаров.");
        }
    }

    // Метод для добавления товара
    private void addProduct(String productName, String productType, boolean isExotic) {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(10));

        // Открываем диалоговое окно для добавления товара
        WebElement addButton = driver.findElement(By.xpath("//button[@data-target='#editModal']"));
        addButton.click();
        logger.info("Открыто диалоговое окно добавления товара.");

        // Заполняем данные в диалоговом окне
        WebElement nameField = driver.findElement(By.xpath("//input[@id='name']"));
        nameField.clear();
        nameField.sendKeys(productName);
        assertEquals(productName, nameField.getAttribute("value"),
                "Наименование продукта должно совпадать.");

        WebElement typeDropdown = driver.findElement(By.xpath("//select[@id='type']"));
        typeDropdown.sendKeys(productType);
        assertEquals("VEGETABLE", typeDropdown.getAttribute("value"),
                "Тип продукта должен быть 'VEGETABLE'.");

        WebElement exoticCheckbox = driver.findElement(By.xpath("//input[@id='exotic']"));
        if (isExotic) {
            exoticCheckbox.click();
        }
        assertEquals(isExotic, exoticCheckbox.isSelected(),
                "Чекбокс 'Экзотический' должен быть " + (isExotic ? "активирован" : "не активирован") + ".");

        // Сохраняем товар
        WebElement saveButton = driver.findElement(By.xpath("//button[@id='save']"));
        saveButton.click();
        logger.info("Продукт '{}' успешно добавлен.", productName);

        // Ожидаем появления товара в списке
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'" + productName + "')]")));
    }
}
