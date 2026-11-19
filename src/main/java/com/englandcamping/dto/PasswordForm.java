package com.englandcamping.dto;

/** 비밀번호 변경 화면에서 받는 값 */
public class PasswordForm {

	private String currentPassword;
	private String newPassword;
	/** 새 비밀번호 한 번 더 (오타 방지) */
	private String newPasswordConfirm;

	public String getCurrentPassword() {
		return currentPassword;
	}

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getNewPasswordConfirm() {
		return newPasswordConfirm;
	}

	public void setNewPasswordConfirm(String newPasswordConfirm) {
		this.newPasswordConfirm = newPasswordConfirm;
	}
}