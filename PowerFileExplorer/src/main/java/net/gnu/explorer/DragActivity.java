//package net.gnu.explorer;
//
//import android.app.Activity;
//import android.content.ClipData;
//import android.graphics.drawable.Drawable;
//import android.os.Bundle;
//import android.view.DragEvent;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.View.DragShadowBuilder;
//import android.view.View.OnDragListener;
//import android.view.View.OnTouchListener;
//import android.view.ViewGroup;
//import android.widget.LinearLayout;
//import android.graphics.*;
//
//public class DragActivity implements OnDragListener {
//	private Drawable enterShape;
//	private Drawable normalShape;
//	OnTouchListener dragListener = new OnTouchListener() {
//		@Override
//		public boolean onTouch(View view, MotionEvent motionEvent) {
//			// start move on a touch event
//			if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//				ClipData data = ClipData.newPlainText("", "");
//				DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
//				view.startDrag(data, shadowBuilder, view, 0);
//				view.setVisibility(View.INVISIBLE);
//				return true;
//			}
//			return false;
//
//		}
//	};
//
//	public DragActivity(View item, ViewGroup... container) {
//		//item.buildDrawingCache();
//		//Create Bitmap
//		//Bitmap cache = item.getDrawingCache();
//		//Save Bitmap
//		//saveBitmap(cache);
//		//item.destroyDrawingCache(); 
//
//		enterShape = item.getResources().getDrawable(R.drawable.shape_droptarget);
//		normalShape = item.getResources().getDrawable(R.drawable.shape);
//
//		item.setOnTouchListener(dragListener);
//		for (ViewGroup vg : container) {
//			vg.setOnDragListener(this);
//		}
//	}
//
//	@Override
//	public boolean onDrag(View v, DragEvent event) {
//		switch (event.getAction()) {
//			case DragEvent.ACTION_DRAG_STARTED:
//				// Do nothing
//				break;
//			case DragEvent.ACTION_DRAG_ENTERED:
//				v.setBackground(enterShape);
//				break;
//			case DragEvent.ACTION_DRAG_EXITED:
//				v.setBackground(normalShape);
//				break;
//			case DragEvent.ACTION_DROP:
//				// view dropped, reassign the view to the new ViewGroup
//				View view = (View) event.getLocalState();
//				ViewGroup owner = (ViewGroup) view.getParent();
//				owner.removeView(view);
//				ViewGroup container = (ViewGroup) v;
//				v.setBackground(normalShape);
//				container.addView(view);
//				view.setVisibility(View.VISIBLE);
//				break;
//			case DragEvent.ACTION_DRAG_ENDED:
//				v.setBackground(normalShape);
//				break;
//			default:
//				break;
//		}
//		return true;
//	}
//}
