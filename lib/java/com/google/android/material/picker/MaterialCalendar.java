/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.picker;

import com.google.android.material.R;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import com.google.android.material.picker.selector.GridSelector;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import java.util.Calendar;

/**
 * Fragment for a days of week {@link Calendar} represented as a header row of days labels and
 * {@link GridView} of days backed by {@link MonthInYearAdapter}.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public final class MaterialCalendar<S> extends Fragment {

  private static final String GRID_SELECTOR_KEY = "GRID_SELECTOR_KEY";

  private MonthInYear monthInYear;
  private MonthInYearAdapter monthInYearAdapter;
  private GridSelector<S> gridSelector;

  /**
   * Creates a {@link MaterialCalendar} with {@link GridSelector#drawCell(View, Calendar)} applied
   * to each cell.
   *
   * @param gridSelector Controls the highlight state of the {@link MaterialCalendar}
   * @param <T> Type returned from selections in this {@link MaterialCalendar} by {@link
   *     MaterialCalendar#getSelection()}
   */
  public static <T> MaterialCalendar<T> newInstance(GridSelector<T> gridSelector) {
    MaterialCalendar<T> materialCalendar = new MaterialCalendar<>();
    Bundle args = new Bundle();
    args.putParcelable(GRID_SELECTOR_KEY, gridSelector);
    materialCalendar.setArguments(args);
    return materialCalendar;
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle bundle) {
    super.onSaveInstanceState(bundle);
    bundle.putParcelable(GRID_SELECTOR_KEY, gridSelector);
  }

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    if (bundle == null) {
      bundle = getArguments();
    }
    gridSelector = bundle.getParcelable(GRID_SELECTOR_KEY);
    Calendar calendar = Calendar.getInstance();
    monthInYear = MonthInYear.create(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
    monthInYearAdapter = new MonthInYearAdapter(getContext(), monthInYear, gridSelector);
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    final View root = layoutInflater.inflate(R.layout.mtrl_calendar, viewGroup, false);
    GridView daysHeader = root.findViewById(R.id.calendar_days_header);
    GridView daysGrid = root.findViewById(R.id.calendar_grid);

    daysHeader.setAdapter(new DaysHeaderAdapter());
    daysHeader.setNumColumns(monthInYear.daysInWeek);
    daysGrid.setAdapter(monthInYearAdapter);
    daysGrid.setNumColumns(monthInYear.daysInWeek);
    daysGrid.setOnItemClickListener(
        new OnItemClickListener() {

          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // The onItemClick interface forces use of a wildcard AdapterView, but
            // GridSelector#onItemClick needs a MonthInYearAdapter.
            // The cast is verified by the instanceof on the Adapter backing the AdapterView.
            if (parent.getAdapter() instanceof MonthInYearAdapter) {
              @SuppressWarnings("unchecked")
              AdapterView<MonthInYearAdapter> calendarGrid =
                  (AdapterView<MonthInYearAdapter>) parent;
              gridSelector.onItemClick(calendarGrid, view, position, id);
              gridSelector.drawSelection(calendarGrid);
            }
            // Allows users of MaterialCalendar to set an OnClickListener
            if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
              root.callOnClick();
            } else {
              root.performClick();
            }
          }
        });
    return root;
  }

  public final S getSelection() {
    return gridSelector.getSelection();
  }
}