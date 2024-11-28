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

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

public class AddVegetableTest {

    WebDriver driver = new ChromeDriver();

    @BeforeEach
    public void setUp () {

        System.setProperty("webdriver.chromedriver.driver","\\src\\test\\resources\\chromedriver.exe");
        driver.manage().window().maximize();

        driver.get("http://localhost:8080");
    }

    @Test
    public void AddVegetable () throws InterruptedException {

        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        WebElement sandButton = driver.findElement(By.xpath("//li[@class='nav-item dropdown']"));
        sandButton.click();

        WebElement productButton = driver.findElement(By.xpath("//a[@href ='/food']"));
        productButton.click();

        WebElement addButton = driver.findElement(By.xpath("//button[@data-target='#editModal']"));
        addButton.click();

        WebElement nameField = driver.findElement(By.xpath("//input[@id='name']"));
        nameField.sendKeys("Огурец");
        assertEquals("Огурец", nameField.getAttribute("value"));

        WebElement typeDropdown = driver.findElement(By.xpath("//select[@id='type']"));
        typeDropdown.sendKeys("Овощ");
        assertEquals("VEGETABLE", typeDropdown.getAttribute("value"));

        WebElement exoticCheckbox = driver.findElement(By.xpath("//input[@id='exotic']"));
        assertFalse(exoticCheckbox.isSelected());

        WebElement saveButton = driver.findElement(By.xpath("//button[@id='save']"));
        saveButton.click();

        WebElement productRow = driver.findElement(By.xpath("//td[contains(text(), 'Огурец')]"));
        assertTrue(productRow.isDisplayed());

        Thread.sleep(500);

        addButton = driver.findElement(By.xpath("//button[@data-target='#editModal']"));
        addButton.click();

        nameField = driver.findElement(By.xpath("//input[@id='name']"));
        nameField.sendKeys("Батат");
        assertEquals("Батат", nameField.getAttribute("value"));

        typeDropdown = driver.findElement(By.xpath("//select[@id='type']"));
        typeDropdown.sendKeys("Овощ");
        assertEquals("VEGETABLE", typeDropdown.getAttribute("value"));

        exoticCheckbox = driver.findElement(By.xpath("//input[@id='exotic']"));
        exoticCheckbox.click();
        assertTrue(exoticCheckbox.isSelected());

        saveButton = driver.findElement(By.xpath("//button[@id='save']"));
        saveButton.click();

        productRow = driver.findElement(By.xpath("//td[contains(text(), 'Батат')]"));
        assertTrue(productRow.isDisplayed());
    }

    @AfterEach
    public void clear() throws InterruptedException{
        WebElement sandboxMenu = driver.findElement(By.xpath("//li[@class='nav-item dropdown']"));
        sandboxMenu.click();

        WebElement sandboxMenuClear = driver.findElement(By.xpath("//a[@id='reset']"));
        sandboxMenuClear.click();
    }
}
