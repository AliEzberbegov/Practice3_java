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
import java.sql.*;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class AddVegetableTest {

    WebDriver driver;

    private static final Logger logger = LoggerFactory.getLogger(AddVegetableTest.class);
    private static final String[] TEST_PRODUCTS = {"Огурец", "Батат"};
    private static final String URL = "http://localhost:8080";
    private static final String DB_URL = "jdbc:h2:tcp://localhost:9092/mem:testdb";
    private static final String DB_USER = "user";
    private static final String DB_PASSWORD = "pass";

    @BeforeEach
    public void setUp() throws SQLException, InterruptedException {

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

            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();

            Thread.sleep(10000);

            //Проверка, что добавляемые продукты отсутствуют в БД
            checkIfProductExistsInDB("Огурец");
            checkIfProductExistsInDB("Батат");

        } catch (IOException | InterruptedException e) {
            logger.error("Ошибка в предусловии: {}", e.getMessage());
        }
    }

    @AfterEach
    public void clear() {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(10));

        try {

            //Удаление товаров из БД
            for (String productName: TEST_PRODUCTS){
                deleteProductsFromDB(productName);
            }

            logger.info("Добавленные товары удалены из БД!");

            //Вывод таблицы продуктов из БД
            productsFromDB();

            //Путь к директории, в которой должен открыться PowerShell
            String path = "C:/Working Project";

            //Команда для завершения работы стенда
            String command = "powershell.exe -NoExit -Command \"Set-Location '"
                    + path + "'; Stop-Process -Name java; exit\"";

            //Завершение работы стенда
            Runtime.getRuntime().exec(command);

        } catch (Exception e) {
            logger.error("Ошибка во время удаления данных из БД: {}", e.getMessage());
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    @Test
    public void AddVegetable() throws InterruptedException, SQLException {

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

            // Проверяем, что товар добавлен на страницу
            WebElement productRow = driver.findElement(
                    By.xpath("//*[contains(text(),'" + productName + "')]"));
            assertTrue(productRow.isDisplayed(),
                    "Продукт " + productName + " должен быть отображен в списке товаров.");

            //Проверяем, что товар добавлен в БД
            checkIfProductExistsInDB(productName);
        }

        //Вывод таблицы товаров из БД
        productsFromDB();

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

    //Метод для проверки наличия товаров в БД
    private void checkIfProductExistsInDB(String productName) throws SQLException {

        //Соединение с БД
        Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        Statement statement = connection.createStatement();

        //Отправка и вывод запроса
        String products = "SELECT * FROM food WHERE food_name IN ('" + productName + "');";
        ResultSet resultSet = statement.executeQuery(products);
        if (resultSet.next()) {
            logger.info("Продукт '" + productName + "' найден в БД.");
        } else {
            logger.info("Продукт '" + productName + "' не найден в БД.");
        }
    }

    //Метод для вывода всей таблицы продуктов БД
    private void productsFromDB() throws SQLException {

        //Соединение с БД
        Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        Statement statement = connection.createStatement();

        //Отправка и вывод запроса
        String products = "SELECT FOOD_ID, FOOD_NAME, FOOD_TYPE, FOOD_EXOTIC from FOOD";
        ResultSet resultSet = statement.executeQuery(products);
        while (resultSet.next()){
            int food_id = resultSet.getInt("FOOD_ID");
            String food_name = resultSet.getString("FOOD_NAME");
            String food_type = resultSet.getString("FOOD_TYPE");
            boolean food_exotic = resultSet.getBoolean("FOOD_EXOTIC");
            logger.info("id:" + food_id +
                    "; name:" + food_name +
                    "; type:" + food_type +
                    "; exotic:" + food_exotic + ".");
        }
    }

    //Метод для удаления товаров из БД
    private void deleteProductsFromDB(String productName) throws SQLException {

        //Соединение с БД
        Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        Statement statement = connection.createStatement();

        //Отправка запроса на удаление данных
        String products = "DELETE FROM food WHERE food_name IN ('" + productName + "');";
        statement.executeUpdate(products);

    }
}