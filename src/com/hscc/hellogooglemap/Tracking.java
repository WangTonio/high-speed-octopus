package com.hscc.hellogooglemap;

import java.util.ArrayList;
import com.google.android.maps.GeoPoint;

public class Tracking {
	AnalysisRawData OriData = new AnalysisRawData();         //�إ� AnalysisRawData
	ArrayList<SenseRecord> forwarding  = new ArrayList();    //forwarding �Ҳ��ͪ����   
	ArrayList<SenseRecord> backwarding = new ArrayList();    //backwarding�Ҳ��ͪ����
	
	public Tracking(){
		// 1. �q RawData ����X�Ҧ��� intersection
		// 2. ��X�����I
		// 3. startTracking
	}
	
	public void startTracking(){
		// 1. ForwardTracking
		// 2. BackwardTracking
		// 3. �P�_ F �����I�� B �����I���Z���O�_�b�X�z�d��
		// 4. �Y�O, �Ҧ� intersection ���I�N�T�w�F
		// 5. �Y�_....
	}
	
	public void ForwardTracking(GeoPoint Start, int StopIndex){
		// 1. �q start point �X�o, ����J�� intersection
		// 2. �q��l��ƪ� GeoPoint of intersection , ��X�a�ϤW������  GeoPoint of intersection
		// 3. ��a�ϤW������ GeoPoint of intersection �s�� RawData ��
		// 4. �O intersection �����s�� start point ���� Step1, ���쨫�� StopIndex
	}
	
	public void BackwardTracking(GeoPoint End, int StopIndex){
		// 1. �q End point �X�o, ����J�� intersection
		// 2. �q��l��ƪ� GeoPoint of intersection , ��X�a�ϤW������  GeoPoint of intersection
		// 3. ��a�ϤW������ GeoPoint of intersection �s�� RawData ��
		// 4. �O intersection �����s�� start point ���� Step1, ���쨫�� StopIndex

	}
}
