package co.fineants.data;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DummyDataCsvGenerator {

	public static void main(String[] args) {
		DummyDataCsvGenerator dummyDataCsvGenerator = new DummyDataCsvGenerator();
		dummyDataCsvGenerator.writeMemberFile();
		dummyDataCsvGenerator.writePortfolioFile();
	}

	public void writeMemberFile() {
		String fileName = "src/main/resources/db/mysql/member.csv";
		CSVFormat csvFormat = CSVFormat.Builder.create()
			.setHeader("id", "email", "nickname", "provider", "password", "profileUrl", "create_at")
			.setSkipHeaderRecord(false)
			.build();
		List<String[]> members = createMemberDummyData();
		boolean result = writeCsvFile(fileName, csvFormat, members);
		if (result) {
			log.info("success writing the member csv file");
		} else {
			log.info("fail writing the member csv file");
		}
	}

	private List<String[]> createMemberDummyData() {
		int recordCount = 1_000_000;
		List<String[]> result = new ArrayList<>();
		for (long i = 1; i <= recordCount; i++) {
			String id = String.valueOf(i);
			String email = String.format("antuser%d@gmail.com", i);
			String nickname = String.format("antuser%d", i);
			String provider = "local";
			String password = "$2a$10$zT6g60wI9rup2EvGbDRKa.D9N3RB5wMoFTlIGAaoZMxqX7R80pPQq";
			String profileUrl = null;
			String createAt = LocalDateTime.now().toString();
			result.add(new String[] {id, email, nickname, provider, password, profileUrl, createAt});
		}
		return result;
	}

	public boolean writeCsvFile(String fileName, CSVFormat csvFormat, List<String[]> data) {
		try (FileWriter out = new FileWriter(fileName)) {
			CSVPrinter printer = new CSVPrinter(out, csvFormat);
			printer.printRecords(data);
		} catch (IOException e) {
			log.error(e.getMessage());
			return false;
		}
		return true;
	}

	private void writePortfolioFile() {
		String fileName = "src/main/resources/db/mysql/portfolio.csv";
		CSVFormat csvFormat = CSVFormat.Builder.create()
			.setHeader("id", "name", "securitiesFirm", "budget", "targetGain", "maximumLoss", "targetGainIsActive",
				"maximumLossIsActive", "createAt", "member_id")
			.setSkipHeaderRecord(false)
			.build();
		List<String[]> portfolios = createPortfolioDummyData();
		boolean result = writeCsvFile(fileName, csvFormat, portfolios);
		if (result) {
			log.info("success writing the portfolio csv file");
		} else {
			log.info("fail writing the portfolio csv file");
		}
	}

	private List<String[]> createPortfolioDummyData() {
		int recordCount = 5_000;
		List<String[]> result = new ArrayList<>();
		for (long i = 1; i <= recordCount; i++) {
			String id = String.valueOf(i);
			String name = String.format("portfolio%d", i);
			String securitiesFirm = "토스증권";
			String budget = "1000000";
			String targetGain = "1500000";
			String maximumLoss = "900000";
			String targetGainIsActive = "true";
			String maximumLossIsActive = "true";
			String createAt = LocalDateTime.now().toString();
			String memberIdString = "1";
			result.add(new String[] {id, name, securitiesFirm, budget, targetGain, maximumLoss, targetGainIsActive,
				maximumLossIsActive, createAt, memberIdString});
		}
		return result;
	}
}
