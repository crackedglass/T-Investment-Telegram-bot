package com.example.tgbotdemo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.example.tgbotdemo.domain.Guild;
import com.example.tgbotdemo.domain.User;
import com.example.tgbotdemo.services.CellService;
import com.example.tgbotdemo.services.GuildService;
import com.example.tgbotdemo.services.OrderService;
import com.example.tgbotdemo.services.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ResourceUtil {
    @Autowired
    private UserService userService;
    @Autowired
    private GuildService guildService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private CellService cellService;

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
            cell.setCellValue(users.get(i).getUsername().toLowerCase());
        }

        try {
            File file = new File("resources/temp/temp.xlsx");
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            workbook.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public File generateDBDump() {
        Workbook wb = new XSSFWorkbook();
        List<Guild> guilds = guildService.findAll();
        for (Guild guild : guilds) {
            Sheet sheet = wb.createSheet(guild.getName());
            List<User> users = guild.getUsers().stream().toList();
            sheet.setColumnWidth(0, 256 * 64);
            sheet.setColumnWidth(1, 256 * 20);
            for (int i = 0; i < users.size(); i++) {
                Row row = sheet.createRow(i);
                Cell username = row.createCell(0);
                Cell money = row.createCell(1);
                User user = users.get(i);
                username.setCellValue(user.getUsername());
                money.setCellValue(user.getMoney());
            }
        }
        Sheet cellSheet = wb.createSheet("Клетки");
        List<com.example.tgbotdemo.domain.Cell> cells = cellService.getAllCells();
        cells.sort((a, b) -> (a.getNumber() > b.getNumber()) ? 1 : -1);
        for (int i = 0; i < cells.size(); i++) {
            com.example.tgbotdemo.domain.Cell cell = cells.get(i);
            Row row = cellSheet.createRow(i);
            Cell cellNumber = row.createCell(0);
            Cell cellGuild = row.createCell(1);
            cellNumber.setCellValue(cell.getNumber());
            Guild ownerGuild = cell.getOwnerGuild();
            if (ownerGuild != null)
                cellGuild.setCellValue(ownerGuild.getName());
        }

        try {
            File file = new File("resources/temp/dbDump.xlsx");
            FileOutputStream outputStream = new FileOutputStream(file);
            wb.write(outputStream);
            wb.close();
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
            FileOutputStream fileOutputStream = new FileOutputStream(
                    new File("resources/temp/temp.xlsx"));
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try (FileInputStream fileStream = new FileInputStream(new File("resources/temp/temp.xlsx"))) {
            Workbook workbook = new XSSFWorkbook(fileStream);
            Sheet sheet = workbook.getSheetAt(0);
            Map<String, Integer> data = new HashMap<>();

            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                String username = row.getCell(0).getStringCellValue().toLowerCase();
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
        File file = new File("resources/templates/users.xlsx");
        try {
            return file;
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
            FileOutputStream fileOutputStream = new FileOutputStream(
                    new File("resources/temp/temp.xlsx"));
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            fileOutputStream.close();
            log.info(String.format("File downloaded"));
            orderService.deleteAll();
            userService.deleteAll();
            guildService.deleteAll();

            FileInputStream fileStream = new FileInputStream(
                    new File("resources/temp/temp.xlsx"));

            Workbook workbook = new XSSFWorkbook(fileStream);
            for (Sheet sheet : workbook) {
                String guildName = sheet.getSheetName();
                Guild guild = new Guild(guildName);
                guildService.save(guild);
                for (Row row : sheet) {
                    try {
                        String username = row.getCell(0).getStringCellValue().toLowerCase();
                        if (username.matches("^\\\\s+$") || username == null || username.equals(""))
                            continue;
                        User user = userService.getByUsername(username);
                        Optional<Cell> moneyCell = Optional.ofNullable(row.getCell(1));
                        int money = 0;
                        if (moneyCell.isPresent())
                            money = (int) moneyCell.get().getNumericCellValue();

                        if (user == null)
                            userService.save(new User(username, money, guild));
                        else {
                            user.setMoney(money);
                            user.setGuild(guild);
                            userService.save(user);
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            }

            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadNewMap(String filePath) throws Exception {

        URL url = new URI(
                String.format("https://api.telegram.org/file/bot%s/%s", environment.getProperty("TOKEN"), filePath))
                .toURL();
        ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(
                new File("resources/images/map.jpg"));
        FileChannel fileChannel = fileOutputStream.getChannel();
        fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        fileOutputStream.close();
        log.info(String.format("New map downloaded"));

    }
}
