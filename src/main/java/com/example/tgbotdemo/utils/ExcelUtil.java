package com.example.tgbotdemo.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.example.tgbotdemo.domain.Guild;
import com.example.tgbotdemo.domain.User;
import com.example.tgbotdemo.services.GuildService;
import com.example.tgbotdemo.services.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ExcelUtil {
    @Autowired
    private UserService userService;
    @Autowired
    private GuildService guildService;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private Environment environment;

    public File generateTableOfUsers() {
        Workbook workbook = new XSSFWorkbook();
        List<User> users = userService.getAllUsers();
        Sheet sheet = workbook.createSheet("Пользователи");
        sheet.setColumnWidth(0, 256 * 64);
        sheet.setColumnWidth(1, 256 * 20);

        for (int i = 0; i < users.size(); i++) {
            Row row = sheet.createRow(i);
            Cell cell = row.createCell(0);
            cell.setCellValue(users.get(i).getUsername());
        }

        try {
            File file = new ClassPathResource("temp/temp.xlsx").getFile();
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            workbook.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void processAddMoneyFile(String filePath) {
        try {
            URL url = new URI(
                    String.format("https://api.telegram.org/file/bot%s/%s", environment.getProperty("TOKEN"), filePath))
                    .toURL();
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream("add.xlsx");
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try (FileInputStream fileStream = new FileInputStream(new File("add.xlsx"))) {
            Workbook workbook = new XSSFWorkbook(fileStream);
            Sheet sheet = workbook.getSheetAt(0);
            Map<String, Integer> data = new HashMap<>();

            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                String username = row.getCell(0).getStringCellValue();
                Optional<Cell> cellToAdd = Optional.ofNullable(row.getCell(1));
                if (cellToAdd.isPresent()) {
                    int toAdd = (int) Math.round(cellToAdd.get().getNumericCellValue());
                    data.put(username, toAdd);
                }
            }
            for (String u : data.keySet()) {
                User user = userService.getByUsername(u);
                if (user == null) {
                    continue;
                }
                user.setMoney(user.getMoney() + data.get(u));
                userService.save(user);
            }
            log.info(data.toString());
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }

    public File getUsersTemplate() {
        Resource resource = resourceLoader.getResource("classpath:templates/users.xlsx");
        try {
            File file = resource.getFile();
            return resource.getFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void loadUsers(String filePath) {
        try {
            URL url = new URI(
                    String.format("https://api.telegram.org/file/bot%s/%s", environment.getProperty("TOKEN"), filePath))
                    .toURL();
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream("new_users.xlsx");
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try (FileInputStream fileStream = new FileInputStream(new File("new_users.xlsx"))) {
            Workbook workbook = new XSSFWorkbook(fileStream);
            int sheets = workbook.getNumberOfSheets();
            for (int i = 1; i < sheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String guildName = sheet.getSheetName();
                Guild guild = new Guild(guildName);
                guildService.save(guild);
                for (Row row : sheet) {
                    String username = row.getCell(0).getStringCellValue();
                    Optional<Cell> moneyCell = Optional.ofNullable(row.getCell(1));
                    if (moneyCell.isPresent()) {
                        int money = (int) moneyCell.get().getNumericCellValue();
                        userService.save(new User(username, money, guild));
                    } else {
                        userService.save(new User(username, 0, guild));
                    }
                }
            }

            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }
}
