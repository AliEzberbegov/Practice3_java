package ibs.test.practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

public class AddFruitTest {

    WebDriver driver = new ChromeDriver();

    @BeforeEach
    public void setUp () {

        System.setProperty("webdriver.chromedriver.driver","\\src\\test\\resources\\chromedriver.exe");
        driver.manage().window().maximize();

        driver.get("http://localhost:8080");
    }

    @Test
    public void AddFruit () throws InterruptedException {

        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        WebElement sandButton = driver.findElement(By.xpath("//li[@class='nav-item dropdown']"));
        sandButton.click();

        WebElement productButton = driver.findElement(By.xpath("//a[@href ='/food']"));
        productButton.click();

        WebElement addButton = driver.findElement(By.xpath("//button[@data-target='#editModal']"));
        addButton.click();

        WebElement nameField = driver.findElement(By.xpath("//input[@id='name']"));
        nameField.sendKeys("Виноград");
        assertEquals("Виноград", nameField.getAttribute("value"));

        WebElement typeDropdown = driver.findElement(By.xpath("//select[@id='type']"));
        typeDropdown.sendKeys("Фрукт");
        assertEquals("FRUIT", typeDropdown.getAttribute("value"));

        WebElement exoticCheckbox = driver.findElement(By.xpath("//input[@id='exotic']"));
        assertFalse(exoticCheckbox.isSelected());

        WebElement saveButton = driver.findElement(By.xpath("//button[@id='save']"));
        saveButton.click();

        WebElement productRow = driver.findElement(By.xpath("//td[contains(text(), 'Виноград')]"));
        assertTrue(productRow.isDisplayed());

        Thread.sleep(500);

        addButton = driver.findElement(By.xpath("//button[@data-target='#editModal']"));
        addButton.click();

        nameField = driver.findElement(By.xpath("//input[@id='name']"));
        nameField.sendKeys("Манго");
        assertEquals("Манго", nameField.getAttribute("value"));

        typeDropdown = driver.findElement(By.xpath("//select[@id='type']"));
        typeDropdown.sendKeys("Фрукт");
        assertEquals("FRUIT", typeDropdown.getAttribute("value"));

        exoticCheckbox = driver.findElement(By.xpath("//input[@id='exotic']"));
        exoticCheckbox.click();
        assertTrue(exoticCheckbox.isSelected());

        saveButton = driver.findElement(By.xpath("//button[@id='save']"));
        saveButton.click();

        productRow = driver.findElement(By.xpath("//td[contains(text(), 'Манго')]"));
        assertTrue(productRow.isDisplayed());
    }

    @AfterEach
    public void checkedData (){
        WebElement sandButton = driver.findElement(By.xpath("//a[@id='navbarDropdown']"));
        sandButton.click();

        WebElement buttonClear = driver.findElement(By.xpath("//a[@id='reset']"));
        buttonClear.click();
    }
}