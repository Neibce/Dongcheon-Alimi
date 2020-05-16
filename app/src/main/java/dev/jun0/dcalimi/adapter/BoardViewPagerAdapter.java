package dev.jun0.dcalimi.adapter;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import dev.jun0.dcalimi.fragment.board.NoticeFragment;
import dev.jun0.dcalimi.fragment.main.BoardFragment;

public class BoardViewPagerAdapter extends FragmentStatePagerAdapter {
    private BoardFragment mBoardFragment;

    public BoardViewPagerAdapter(@NonNull FragmentManager fm, BoardFragment boardFragment) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mBoardFragment = boardFragment;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return NoticeFragment.newInstance(mBoardFragment);
            case 1:
                return NoticeFragment.newInstance(mBoardFragment);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }


}