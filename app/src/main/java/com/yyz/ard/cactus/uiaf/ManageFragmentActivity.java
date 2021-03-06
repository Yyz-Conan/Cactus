package com.yyz.ard.cactus.uiaf;


import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.util.Stack;

/**
 * 该类集成了Fragment管理全套方法
 *
 * @author yyz
 * @date 7/8/2016.
 */
public abstract class ManageFragmentActivity extends BaseActivity {

    protected Stack<BaseFragment> fragmentStack = new Stack<>();
    private int currentFragmentIndex = -1;


    /**
     * 创建新的fragment并显示出来
     *
     * @param fragment        要显示的fragment
     * @param containerViewId 布局文件id
     */
    public <T> T showFragment(BaseFragment fragment, @IdRes int containerViewId) {
        return showFragment(fragment, null, containerViewId);
    }

    public <T> T showFragment(Class<? extends BaseFragment> cls, @IdRes int containerViewId) {
        return showFragment(cls, null, containerViewId);
    }

    /**
     * 创建新的fragment并显示出来
     *
     * @param cls             要显示的fragment
     * @param bundle          传递给fragment的数据
     * @param containerViewId 布局文件id
     */
    public <T> T showFragment(Class<? extends BaseFragment> cls, Bundle bundle, @IdRes int containerViewId) {
        if (cls != null) {
            BaseFragment fragment = null;
            try {
                fragment = findFragment(cls.getName());
                if (fragment == null) {
                    fragment = cls.newInstance();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return showFragment(fragment, bundle, containerViewId);
        }
        return null;
    }

    /**
     * 创建新的fragment并显示出来
     *
     * @param fragment        要显示的fragment
     * @param bundle          传递给fragment的数据
     * @param containerViewId 布局文件id
     */
    public <T> T showFragment(BaseFragment fragment, Bundle bundle, @IdRes int containerViewId) {
        if (fragment != null) {
            if (currentFragmentIndex > -1) {
                BaseFragment lastFragment = fragmentStack.get(currentFragmentIndex);
                if (fragment == lastFragment) {
                    //当前的fragment跟要显示的fragment相同则不需要任何处理
                    return (T) fragment;
                }
                hideFragment(lastFragment);
            }
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            if (!fragmentStack.contains(fragment)) {
                String fragmentTag = fragment.getClass().getName();
                fragment.setArguments(bundle);
                fragmentTransaction.add(containerViewId, fragment, fragmentTag);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    fragmentTransaction.commitNowAllowingStateLoss();
                } else {
                    fragmentTransaction.commitAllowingStateLoss();
                }
                getFragmentManager().executePendingTransactions();
                fragmentStack.add(fragment);
                currentFragmentIndex = fragmentStack.size() - 1;
            } else {
                fragmentTransaction.show(fragment);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    fragmentTransaction.commitNowAllowingStateLoss();
                } else {
                    fragmentTransaction.commitAllowingStateLoss();
                }
                getFragmentManager().executePendingTransactions();
                currentFragmentIndex = findFragmentIndex(fragment.getClass().getName());
            }
        }
        return (T) fragment;
    }

    // -------------- showFragment --------------------

    /**
     * 隐藏fragment
     *
     * @param fragment 要隐藏fragment
     */
    private void hideFragment(BaseFragment fragment) {
        if (fragment != null) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.hide(fragment);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fragmentTransaction.commitNowAllowingStateLoss();
            } else {
                fragmentTransaction.commitAllowingStateLoss();
            }
            getFragmentManager().executePendingTransactions();
        }
    }

    // -------------- hideFragment --------------------

    /**
     * 返回上一个fragment界面
     */
    public void backFragment() {
        backFragment(null);
    }

    public void backFragment(Bundle bundle) {
        popFragment(currentFragmentIndex - 1);
        showLastFragment(bundle);
    }


    // -------------- backFragment --------------------

    public void backToFirstFragment() {
        backToFirstFragment(null);
    }

    /**
     * 返回到最底部fragment界面
     */
    public void backToFirstFragment(Bundle bundle) {
        popFragment(0);
        showLastFragment(bundle);
    }

    // -------------- backToFirstFragment --------------------

    /**
     * 退到指定的Fragment 并显示它
     *
     * @param fragmentIndex
     */
    public void backFragment(int fragmentIndex, Bundle bundle) {
        popFragment(fragmentIndex);
        showLastFragment(bundle);
    }

    // -------------- backFragment --------------------


    /**
     * 查找Stack中判断是否存在指定的Fragment
     *
     * @param fragmentName 指定搜索的Fragment
     * @return 不存在返回-1，存在返回指定Fragment的在Stack的索引
     */
    public int findFragmentIndex(String fragmentName) {
        int index = -1;
        if (fragmentName != null) {
            for (int len = 0; len < fragmentStack.size(); len++) {
                BaseFragment fragment = fragmentStack.get(len);
                String tag = fragment.getTag();
                if (fragmentName.equals(tag)) {
                    index = len;
                    break;
                }
            }
        }
        return index;
    }

    // -------------- findFragmentIndex --------------------

    /**
     * 查找stack中目标Fragment
     *
     * @param fragmentName 目标Fragment名字
     * @return 找不到返回null
     */
    public BaseFragment findFragment(String fragmentName) {
        BaseFragment baseFragment = null;
        int index = findFragmentIndex(fragmentName);
        if (index != -1) {
            baseFragment = fragmentStack.get(index);
        }
        return baseFragment;
    }

    // -------------- findFragment --------------------


    /**
     * 退到指定的界面
     *
     * @param index 往后退索引值
     */
    protected void popFragment(int index) {
        if (index >= -1) {
            index++;
            int size = fragmentStack.size() - index;
            for (int count = 0; count < size; count++) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.remove(fragmentStack.pop());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    fragmentTransaction.commitNowAllowingStateLoss();
                } else {
                    fragmentTransaction.commitAllowingStateLoss();
                }
                getFragmentManager().executePendingTransactions();
            }
        }
    }

    // -------------- popFragment --------------------


    /**
     * 显示栈顶的fragment，并回调onRefreshFragment()
     *
     * @param bundle
     */
    private void showLastFragment(Bundle bundle) {
        int size = fragmentStack.size();
        if (size > 0) {
            BaseFragment fragment = fragmentStack.peek();
            fragment.setArguments(bundle);
            if (hasWindowFocus()) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.show(fragment);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    fragmentTransaction.commitNowAllowingStateLoss();
                } else {
                    fragmentTransaction.commitAllowingStateLoss();
                }
                getFragmentManager().executePendingTransactions();
                currentFragmentIndex = size - 1;
            }
        } else {
            currentFragmentIndex = -1;
        }
    }

    // -------------- showHasFragment --------------------

    /**
     * 返回栈中最后进来的数据
     *
     * @return 返回一个Fragment
     */
    public BaseFragment getCurrentFragment() {
        BaseFragment baseFragment = null;
        if (!fragmentStack.isEmpty()) {
            baseFragment = fragmentStack.get(currentFragmentIndex);
        }
        return baseFragment;
    }

    // -------------- getCurrentFragment --------------------


    /**
     * 获取Fragment栈的大小
     *
     * @return
     */
    public int getStackSize() {
        return fragmentStack.size();
    }

    // -------------- getStackSize --------------------

    /**
     * 退出整个应用
     */
    protected void exit() {
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    // -------------- exit --------------------

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        BaseFragment baseFragment = getCurrentFragment();
        if (baseFragment != null) {
            if (baseFragment.onKeyDown(keyCode, event)) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        BaseFragment baseFragment = getCurrentFragment();
        if (baseFragment != null) {
            baseFragment.onBackPressed();
        }
        super.onBackPressed();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        BaseFragment baseFragment = getCurrentFragment();
        if (baseFragment != null) {
            baseFragment.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }


    // -------------- onKeyDown onBackPressed onTouchEvent--------------------

    @Override
    protected void onDestroy() {
        fragmentStack.clear();
        super.onDestroy();
    }
}
