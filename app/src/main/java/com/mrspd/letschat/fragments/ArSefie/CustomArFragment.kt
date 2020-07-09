package com.mrspd.letschat.fragments.ArSefie

import android.Manifest
import com.google.ar.sceneform.ux.ArFragment

class CustomArFragment : ArFragment() {

    override fun getAdditionalPermissions(): Array<String> {
        val additionalPermissions = super.getAdditionalPermissions()
        val permissionsLength = additionalPermissions.size
        val permissions = Array(permissionsLength + 1) { Manifest.permission.WRITE_EXTERNAL_STORAGE }
        if(permissionsLength > 0) {
            System.arraycopy(additionalPermissions, 0, permissions, 1, permissionsLength)
        }
        return permissions
    }
}