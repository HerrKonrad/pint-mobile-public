package com.example.mobilepint.configuracoes

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.example.mobilepint.R

class FragmentConfiguracoes : Fragment() {

    private lateinit var toast: Toast
    private var lastBackPressTime: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_configuracoes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val txt_editar_perfil = view.findViewById(R.id.configuracoes_txt_editar_perfil) as TextView
        val txt_versao = view.findViewById(R.id.configuracoes_txt_versao) as TextView

        txt_editar_perfil.setOnClickListener {
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentDetalhesPerfil())
            fragment.commit()
        }

        val packageInfo = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, PackageManager.GET_META_DATA)
        val versionName = packageInfo.versionName
        txt_versao.text = getString(R.string.versao_app, versionName)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val currentTime = System.currentTimeMillis()
            if (lastBackPressTime < currentTime - 4000) {
                toast = Toast.makeText(requireContext(), "Pressione novamente para sair.", Toast.LENGTH_LONG)
                toast.show()
                lastBackPressTime = currentTime
            } else {
                toast.cancel()
                requireActivity().finish()
            }
        }
    }
}