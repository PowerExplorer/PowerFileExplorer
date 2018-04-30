package net.gnu.explorer;

import java.io.*;
import dalvik.system.*;
import java.util.*;
import net.gnu.util.*;
import android.app.*;
import com.google.classysharkandroid.dex.*;

public class FillClassesNamesThread extends Thread {
	
	private final byte[] bytes;
	private final ContentFragment fileFrag;
	//private final ArrayList<String> classesList = new ArrayList<>();
	private final String sb1, sb2, sb3;
	private final File f;
	
	public FillClassesNamesThread(final ContentFragment fileFrag, final byte[] bytes, final File f, final String sb1, final String sb2, final String sb3) {
		this.bytes = bytes;
		this.fileFrag = fileFrag;
		this.sb1 = sb1;
		this.sb2 = sb2;
		this.sb3 = sb3;
		this.f = f;
	}

	@Override
	public void run() {
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		final int methodCount = countMethod();
		fileFrag.activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final StringBuilder sb = new StringBuilder(sb1);
					sb.append("\nMethod count " + methodCount);
					sb.append(sb2).append(sb3);
					final String fPath = ExplorerApplication.PRIVATE_PATH + "/" + f.getName() + ".html";
					try {
						FileUtil.writeFileAsCharset(new File(fPath), sb.toString(), "utf-8");
					} catch (IOException e) {
						e.printStackTrace();
					}
					final SlidingTabsFragment.PagerAdapter pagerAdapter;
					final SlidingTabsFragment slidingTabsFragment;
					if (fileFrag.slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
						pagerAdapter = fileFrag.activity.slideFrag2.pagerAdapter;
						slidingTabsFragment = fileFrag.activity.slideFrag2;
					} else {
						pagerAdapter = fileFrag.activity.slideFrag.pagerAdapter;
						slidingTabsFragment = fileFrag.activity.slideFrag;
					}
					final int tabIndex2 = SlidingTabsFragment.getFragTypeIndex(fileFrag, Frag.TYPE.WEB);
					if (tabIndex2 >= 0) {
						pagerAdapter.getItem(tabIndex2).load(fPath);
						slidingTabsFragment.setCurrentItem(tabIndex2, true);
					} else {
						fileFrag.listView.postDelayed(new Runnable() {
								@Override
								public void run() {
									slidingTabsFragment.addTab(Frag.TYPE.WEB, fPath);
									fileFrag.listView.postDelayed(new Runnable() {
											@Override
											public void run() {
												pagerAdapter.getItem(slidingTabsFragment.pageSelected).load(fPath);
											}
										}, 50);
								}
							}, 100);
					}
//					fileFrag.slideFrag2.pagerAdapter.getItem(Frag.TYPE.WEB.ordinal()).load(fPath);
//					fileFrag.slideFrag2.setCurrentItem(Frag.TYPE.WEB.ordinal(), true);
				}
			});
	}

	int countMethod() {
		int methodCount = 0;
		try {
			
			final File incomeFile = File.createTempFile("classes" + Thread.currentThread().getId(), ".dex", fileFrag.activity.getCacheDir());

			FileUtil.bArr2File(bytes, incomeFile.getAbsolutePath());
			
			final File optimizedFile = File.createTempFile("opt" + Thread.currentThread().getId(), ".dex", fileFrag.activity.getCacheDir());

			final DexFile dx = DexFile.loadDex(incomeFile.getPath(),
										 optimizedFile.getPath(), 0);
			final Enumeration<String> classNames = dx.entries();
			final DexClassLoader loader = DexLoaderBuilder.fromBytes(fileFrag.activity, bytes);
			
			Class<?> loadClass;
			while (classNames.hasMoreElements()) {
				try {
					loadClass = loader.loadClass(classNames.nextElement());
//					Reflector reflector = new Reflector(loadClass);
//					reflector.generateClassData();
//					String result = reflector.toString();
					methodCount += loadClass.getConstructors().length;
					methodCount += loadClass.getMethods().length;
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			incomeFile.delete();
			optimizedFile.delete();
		} catch (Exception e) {
			// ODEX, need to see how to handle
			e.printStackTrace();
		}
		return methodCount;
	}
}
