package tw.idv.ian.shiftArrangement;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Hello world!
 *
 */
public class App {

	public static final int arrangeMonth = 10;
	public static final int arrangeYear = 2015;
	public static final String theLastOneDrCode = "D";
	public static final String theLastTwoDrCode = "B";
	public static final String theLastFinalWeekMondayDrCode = "C";
	public static final String theLastFinalWeekThursdayDrCode = "";
	public static boolean isTheSameDr = false;
	public static int totalCalculation = 0;
	public static HashSet<String> haveSatShiftDrs;
	public static HashSet<String> haveSunShiftDrs;
	public Calendar cal = this.getArrangeCalendar(arrangeMonth, arrangeYear);
	public static final Map<String, Doctor> DOCTORS = new HashMap<>();
	public Map<Integer, List<String>> exceptDaysMap = new TreeMap<>();
	private Doctor drA = new Doctor(2, "林醫師", "A");
	private Doctor drB = new Doctor(2, "陳醫師", "B");
	private Doctor drC = new Doctor(1, "卓醫師", "C");
	private Doctor drD = new Doctor(1, "歐陽醫師", "D");
	private Map<Integer, String> arranedShift;
	private boolean reCalcu = false;
	private int totalShiftA = 0;
	private int totalShiftB = 0;
	private int totalShiftC = 0;
	private int totalShiftD = 0;

	public static void main(String[] args) {
		long start = new Date().getTime();
		System.out.println("Start >> " + start);
		new App().go();
		long end = new Date().getTime();
		System.out.println();
		System.out.println("End >> " + end + ", 經過  >> " + (end - start));
	}

	private void initializaParam() {
		haveSatShiftDrs = new HashSet<>();
		haveSunShiftDrs = new HashSet<>();
		arranedShift = new HashMap<>();
		totalShiftA = 0;
		totalShiftB = 0;
		totalShiftC = 0;
		totalShiftD = 0;
	}

	private void initialDoctors() {
		DOCTORS.put(drA.getCode(), drA);
		DOCTORS.put(drB.getCode(), drB);
		DOCTORS.put(drC.getCode(), drC);
		DOCTORS.put(drD.getCode(), drD);
	}

	private void initialExceptDays() {
		Integer[] expDrA = { 3, 10, 12, 26 };
		Integer[] expDrB = { 12, 13, 30 };
		Integer[] expDrC = { 10, 11, 25 };
//		Integer[] expDrC = { 10, 11};
		Integer[] expDrD = { 12, 25, 26, 27, 28 };
//		Integer[] expDrD = { 25, 26, 27, 28 };
		Set<Integer> exceptDaysSet = new TreeSet<>(Arrays.asList(expDrA));
		exceptDaysSet.addAll(Arrays.asList(expDrB));
		exceptDaysSet.addAll(Arrays.asList(expDrC));
		exceptDaysSet.addAll(Arrays.asList(expDrD));
		for (Integer day : exceptDaysSet) {
			List<String> temp = new ArrayList<>();
			if (Arrays.asList(expDrA).contains(day)) {
				temp.add(drA.getCode());
			}

			if (Arrays.asList(expDrB).contains(day)) {
				temp.add(drB.getCode());
			}

			if (Arrays.asList(expDrC).contains(day)) {
				temp.add(drC.getCode());
			}

			if (Arrays.asList(expDrD).contains(day)) {
				temp.add(drD.getCode());
			}
			exceptDaysMap.put(day, temp);
		}
	}

	private void checkTheLastWeekShiftDrIsTheSame() {
		isTheSameDr = theLastFinalWeekThursdayDrCode
				.equalsIgnoreCase(theLastFinalWeekMondayDrCode);
	}

	public void go() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy 年 MM 月");
		String sdfDate = sdf.format(cal.getTime());
		this.initialDoctors();
		this.initialExceptDays();
		this.checkTheLastWeekShiftDrIsTheSame();
		do {
			totalCalculation++;
			reCalcu = false;
			try {
				this.initializaParam();
				this.arrangeShift(cal);
			} catch (TooManyShiftsException te) {
			} catch (SeniorDontLikeException se) {
			}
		} while (reCalcu);
		System.out.println();
		System.out.println("                 [ " + sdfDate + " ]");
		this.drawCalendar(cal, arranedShift);
		System.out.println();
		System.out.println();

		this.printFinalInfo();

	}

	private void printFinalInfo() {
		System.out.println(">> 本次排班計算共經過  " + totalCalculation + " 次演算！");
		System.out.println();
		DOCTORS.forEach((k, v) -> {
			System.out.print(k + ":" + v.getName() + " (Pri " + v.getPriority()
					+ ") - Total shifts -> ");
			switch (k) {
			case "A":
				System.out.println(totalShiftA);
				break;
			case "B":
				System.out.println(totalShiftB);
				break;
			case "C":
				System.out.println(totalShiftC);
				break;
			case "D":
				System.out.println(totalShiftD);
				break;
			}
		});
	}

	private void arrangeShift(Calendar now) throws TooManyShiftsException,
			SeniorDontLikeException {
		int days = now.getActualMaximum(Calendar.DATE);
		for (int i = 1; i <= days; i++) {
			now.set(Calendar.DAY_OF_MONTH, i);
			boolean isSunday = (now.get(Calendar.DAY_OF_WEEK)) == 1 ? true
					: false;
			boolean isSaturday = (now.get(Calendar.DAY_OF_WEEK)) == 7 ? true
					: false;
			String candidate = this.calcuCandidate(i, arranedShift, isSunday,
					isSaturday);
			arranedShift.put(i, candidate);
		}
		this.countAllShift(arranedShift);
		this.checkTotalArrangeShifts();
	}

	private void checkTotalArrangeShifts() throws SeniorDontLikeException {
		if ((totalShiftA > totalShiftC) || (totalShiftA > totalShiftD)
				|| (totalShiftB > totalShiftC) || (totalShiftB > totalShiftD)) {
			reCalcu = true;
			throw new SeniorDontLikeException();
		} 
		else if (Math.abs(totalShiftD - totalShiftC) > 1) {
			reCalcu = true;
			System.out.println("打掉重練 Part II " + totalCalculation + " 次！ ["
					+ Math.abs(totalShiftD - totalShiftC) + "]");
			throw new SeniorDontLikeException();
		}
	}

	private List<Doctor> genDefaultCandidates() throws TooManyShiftsException {
		List<Doctor> result = new ArrayList<>();
		DOCTORS.forEach((k, v) -> result.add(v));
		this.checkCandidatesSize(result);
		return result;
	}

	private void dealAvoidingExceptionDayThifts(int day, List<Doctor> candidates)
			throws TooManyShiftsException {
		if (exceptDaysMap.containsKey(day)) {
			exceptDaysMap.get(day).forEach(s -> {
				candidates.removeIf(d -> d.getCode().equalsIgnoreCase(s));
			});
		}
		this.checkCandidatesSize(candidates);
	}

	private void dealAvoidingContinusThifts(int day, List<Doctor> candidates,
			Map<Integer, String> arrangingShift) throws TooManyShiftsException {
		if (day == 1) {
			candidates.removeIf(s -> s.getCode().equalsIgnoreCase(
					theLastOneDrCode));
			candidates.removeIf(s -> s.getCode().equalsIgnoreCase(
					theLastTwoDrCode));
		} else if (day == 2) {
			candidates.removeIf(s -> s.getCode().equalsIgnoreCase(
					theLastOneDrCode));
			candidates.removeIf(s -> s.getCode().equalsIgnoreCase(
					arrangingShift.get(day - 1)));
		} else {
			candidates.removeIf(s -> s.getCode().equalsIgnoreCase(
					arrangingShift.get(day - 1)));
			candidates.removeIf(s -> s.getCode().equalsIgnoreCase(
					arrangingShift.get(day - 2)));
		}

		this.checkCandidatesSize(candidates);
	}

	private void dealAvoidingThreeShiftAWeek(int day, List<Doctor> candidates,
			Map<Integer, String> arrangingShift) throws TooManyShiftsException {
		if (day < 7) {
			if (isTheSameDr) {
				candidates.removeIf(s -> s.getCode().equalsIgnoreCase(
						theLastFinalWeekThursdayDrCode));
			}
		} else {
			candidates.removeIf(s -> s.getCode().equalsIgnoreCase(
					arrangingShift.get(day - 6)));
		}
		this.checkCandidatesSize(candidates);
	}

	private void checkCandidatesSize(List<Doctor> candidates)
			throws TooManyShiftsException {
		if (candidates.size() < 1) {
			reCalcu = true;
			throw new TooManyShiftsException();
		}
	}

	private void dealAvoidingWeekendShiftTwice(List<Doctor> candidates,
			boolean isSunday) throws TooManyShiftsException {
		HashSet<String> haveWeekendShiftDrs = (isSunday) ? haveSunShiftDrs
				: haveSatShiftDrs;
		if (haveWeekendShiftDrs.size() < 4) {
			haveWeekendShiftDrs.forEach(s -> {
				candidates.removeIf(v -> v.getCode().equalsIgnoreCase(s));
			});
		}
		this.checkCandidatesSize(candidates);
	}

	private String calcuCandidate(int day, Map<Integer, String> arrangingShift,
			boolean isSunday, boolean isSaturday) throws TooManyShiftsException {
		List<Doctor> candidates = this.genDefaultCandidates();
		if (isSunday) {
			this.dealAvoidingThreeShiftAWeek(day, candidates, arrangingShift);
		}
		this.dealAvoidingContinusThifts(day, candidates, arrangingShift);

		this.dealAvoidingExceptionDayThifts(day, candidates);

		if ((haveSunShiftDrs.size() > 0 && isSunday)
				|| (haveSatShiftDrs.size() > 0 && isSaturday)) {
			this.dealAvoidingWeekendShiftTwice(candidates, isSunday);
		}

		String candidateCode = this.randomCandidate(candidates);
		if (isSunday) {
			haveSunShiftDrs.add(candidateCode);
		}
		if (isSaturday) {
			haveSatShiftDrs.add(candidateCode);
		}
		return candidateCode;
	}

	private String randomCandidate(List<Doctor> candidates)
			throws TooManyShiftsException {
		Random random = new Random();
		int index = random.nextInt(candidates.size());
		return candidates.get(index).getCode();
	}

	// 目前可能會算不出來
	private String randomCandidateWithPri(List<Doctor> candidates)
			throws TooManyShiftsException {
		String candidate = "";
		Random random = new Random();
		List<Doctor> priOneCandidates = candidates.stream()
				.filter(s -> s.getPriority() == 1).collect(Collectors.toList());
		List<Doctor> priTwoCandidates = candidates.stream()
				.filter(s -> s.getPriority() == 2).collect(Collectors.toList());
		if (priOneCandidates.size() > 0) {
			int index = random.nextInt(priOneCandidates.size());
			candidate = priOneCandidates.get(index).getCode();
		} else {
			int index = random.nextInt(priTwoCandidates.size());
			candidate = priTwoCandidates.get(index).getCode();
		}
		return candidate;
	}

	private Calendar getArrangeCalendar(int arrangeMonth, int arrangeYear) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, arrangeMonth - 1);
		cal.set(Calendar.YEAR, arrangeYear);
		return cal;
	}

	private void drawCalendar(Calendar now, Map<Integer, String> finalShift) {
		int month = now.get(Calendar.MONTH);
		now.set(Calendar.DAY_OF_MONTH, 1);
		int week = now.get(Calendar.DAY_OF_WEEK);
		System.out.println("日\t  一\t  二\t  三\t  四\t  五\t  六");
		System.out
				.println("-------------------------------------------------------");

		for (int i = Calendar.SUNDAY; i < week; i++) {
			System.out.print("\t");
		}
		while (now.get(Calendar.MONTH) == month) {
			int day = now.get(Calendar.DAY_OF_MONTH);
			finalShift.forEach((k, v) -> {
				if (day < 10 && day == k) {
					System.out.print(" " + day + " :" + v + "\t");
				} else if (day >= 10 && day == k) {
					System.out.print("" + day + " :" + v + "\t");
				}
			});
			if (week == Calendar.SATURDAY) {
				System.out.println();
				System.out.println();
			}
			now.add(Calendar.DAY_OF_MONTH, 1);
			week = now.get(Calendar.DAY_OF_WEEK);
		}
	}

	private void countAllShift(Map<Integer, String> arrangedShift) {
		arrangedShift.forEach((k, v) -> {
			switch (v) {
			case "A":
				totalShiftA++;
				break;
			case "B":
				totalShiftB++;
				break;
			case "C":
				totalShiftC++;
				break;
			case "D":
				totalShiftD++;
				break;
			}
		});
	}
}
