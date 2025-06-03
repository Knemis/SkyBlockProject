#!/bin/bash

# === Loglama Fonksiyonu ===
# Mesajları zaman damgasıyla birlikte loglar.
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] [$1] - $2"
}

# === YAPILANDIRMA ===
# 1. Ana multiversion dizininizin yolu (Bash ortamınıza uygun formatta)
#    Git Bash veya WSL için örnek: "/c/Users/Lenovo/IdeaProjects/SkyBlockProject/src/main/java/com/knemis/skyblock/skyblockcoreproject/secondcore/multiversion"
#    VEYA scripti doğrudan 'multiversion' klasörünün içinde çalıştıracaksanız "." olarak ayarlayın.
MULTIVERSION_ANA_DIZINI="/c/Users/Lenovo/IdeaProjects/SkyBlockProject/src/main/java/com/knemis/skyblock/skyblockcoreproject/secondcore/multiversion"

# 2. HER BİR modül/sürüm klasörünün İÇİNDE bulunan ve içeriği yukarı taşınıp
#    KENDİSİ SİLİNECEK olan alt klasörün ADI.
#    BU DEĞERİ KENDİ PROJENİZE GÖRE MUTLAKA DEĞİŞTİRİN!
TASINACAK_IC_KLASOR_ADI="BURAYI_DEĞİŞTİRİN_örneğin_core" # ÖNEMLİ: Bu adı doğru şekilde ayarlayın!
# === YAPILANDIRMA SONU ===

log "INFO" "Script başlatılıyor..."
log "CONFIG" "MULTIVERSION_ANA_DIZINI: '${MULTIVERSION_ANA_DIZINI}'"
log "CONFIG" "TASINACAK_IC_KLASOR_ADI: '${TASINACAK_IC_KLASOR_ADI}'"
echo "--------------------------------------------------------------------"
echo "☢️☢️☢️ UYARI! BU SCRİPT GERİ ALINAMAZ DEĞİŞİKLİKLER YAPACAKTIR! ☢️☢️☢️"
echo "Belirtilen ana dizin altındaki her bir alt dizin (modül/sürüm klasörü) içinde,"
echo "'${TASINACAK_IC_KLASOR_ADI}' adında bir klasör aranacak. Bulunursa:"
echo "  1. '${TASINACAK_IC_KLASOR_ADI}' klasörünün TÜM İÇERİĞİ bir üst seviyeye (modül/sürüm klasörüne) taşınacak."
echo "  2. Ardından '${TASINACAK_IC_KLASOR_ADI}' klasörü SİLİNECEK."
echo "Bu işlem Java paket yapılarını bozabilir ve kodunuzu kullanılamaz hale getirebilir."
echo "DEVAM ETMEDEN ÖNCE PROJENİZİN TAM BİR YEDEĞİNİ ALDIĞINIZDAN EMİN OLUN!"
echo "--------------------------------------------------------------------"

if [ "${TASINACAK_IC_KLASOR_ADI}" == "BURAYI_DEĞİŞTİRİN_örneğin_core" ] || [ -z "${TASINACAK_IC_KLASOR_ADI}" ]; then
    log "ERROR" "Lütfen script içindeki 'TASINACAK_IC_KLASOR_ADI' değişkenini projenize uygun bir klasör adıyla güncelleyin."
    read -p "Çıkmak için [Enter] tuşuna basın..." # Hata durumunda da bekle
    exit 1
fi

read -p "Yukarıdaki açıklamaları okudum, anladım ve tüm riskleri kabul ediyorum. İşleme devam etmek istiyor musunuz? (evet/hayır): " confirmation

if [[ ! "$confirmation" =~ ^[Ee][Vv][Ee][Tt]$ ]]; then
    log "INFO" "İşlem kullanıcı tarafından iptal edildi."
    read -p "Çıkmak için [Enter] tuşuna basın..." # İptal durumunda da bekle
    exit 0
fi

if [ ! -d "${MULTIVERSION_ANA_DIZINI}" ]; then
    log "ERROR" "Belirtilen ana multiversion dizini bulunamadı: '${MULTIVERSION_ANA_DIZINI}'"
    read -p "Çıkmak için [Enter] tuşuna basın..." # Hata durumunda da bekle
    exit 1
fi

log "INFO" "İşlem başlıyor. Ana dizin taranacak: '${MULTIVERSION_ANA_DIZINI}'"
processed_module_count=0
# `read -r` kullanmak, satır sonundaki boşluklar veya ters eğik çizgilerle ilgili sorunları önler.
find "${MULTIVERSION_ANA_DIZINI}" -mindepth 1 -maxdepth 1 -type d | while read -r MODUL_VEYA_SURUM_KLASORU; do
    processed_module_count=$((processed_module_count + 1))
    log "MODULE_LOOP" "-----------------------------------------------------"
    log "MODULE_LOOP" "İşleniyor (${processed_module_count}. modül/sürüm): '${MODUL_VEYA_SURUM_KLASORU}'"

    TAM_IC_KLASOR_YOLU="${MODUL_VEYA_SURUM_KLASORU}/${TASINACAK_IC_KLASOR_ADI}"
    log "MODULE_LOOP" "  Beklenen iç klasör yolu: '${TAM_IC_KLASOR_YOLU}'"

    if [ -d "${TAM_IC_KLASOR_YOLU}" ]; then
        log "MODULE_ACTION" "  BULUNDU: İç klasör '${TAM_IC_KLASOR_YOLU}'. İşlem yapılacak."

        can_attempt_rmdir=false
        subshell_rc=99 

        ( 
            log "SUB_SHELL" "    Attempting to cd into '${TAM_IC_KLASOR_YOLU}'"
            cd "${TAM_IC_KLASOR_YOLU}" || { log "SUB_SHELL" "    ERROR: Failed to cd into '${TAM_IC_KLASOR_YOLU}'."; exit 1; }
            log "SUB_SHELL" "    Successfully cd'd into '${TAM_IC_KLASOR_YOLU}'"

            shopt -s dotglob nullglob 
            items_to_move=(*)
            log "SUB_SHELL" "    Found ${#items_to_move[@]} item(s) in '${TASINACAK_IC_KLASOR_ADI}':"
            if [ ${#items_to_move[@]} -gt 0 ]; then
                for item in "${items_to_move[@]}"; do
                    log "SUB_SHELL" "      - $item"
                done
                log "SUB_SHELL" "    Attempting to move item(s) to parent directory ('../')."
                mv -- "${items_to_move[@]}" ../
                mv_rc=$?
                if [ ${mv_rc} -eq 0 ]; then
                    log "SUB_SHELL" "    SUCCESS: Item(s) successfully moved."
                    exit 0 
                else
                    log "SUB_SHELL" "    ERROR: 'mv' command failed with exit code ${mv_rc}. Some/all item(s) might not have been moved."
                    exit 2 
                fi
            else
                log "SUB_SHELL" "    No items to move in '${TASINACAK_IC_KLASOR_ADI}'."
                exit 3 
            fi
        ) 
        subshell_rc=$? 

        case ${subshell_rc} in
            0)
                log "MODULE_ACTION" "  MOVE_RESULT: İçerik başarıyla taşındı."
                can_attempt_rmdir=true
                ;;
            1)
                log "MODULE_ACTION" "  MOVE_RESULT_ERROR: '${TAM_IC_KLASOR_YOLU}' içine girilemedi. 'rmdir' denenmeyecek."
                can_attempt_rmdir=false
                ;;
            2)
                log "MODULE_ACTION" "  MOVE_RESULT_ERROR: Taşıma ('mv') işlemi '${TAM_IC_KLASOR_YOLU}' içinde başarısız oldu. İçerik kısmen taşınmış olabilir. Güvenlik için 'rmdir' denenmeyecek."
                can_attempt_rmdir=false
                ;;
            3)
                log "MODULE_ACTION" "  MOVE_RESULT: '${TAM_IC_KLASOR_YOLU}' içinde taşınacak öğe yoktu. Klasör boş olabilir."
                can_attempt_rmdir=true 
                ;;
            *)
                log "MODULE_ACTION" "  MOVE_RESULT_ERROR: Bilinmeyen alt kabuk durumu: ${subshell_rc}. Güvenlik için 'rmdir' denenmeyecek."
                can_attempt_rmdir=false
                ;;
        esac

        if ${can_attempt_rmdir}; then
            log "MODULE_ACTION" "  Attempting rmdir for '${TAM_IC_KLASOR_YOLU}'"
            rmdir "${TAM_IC_KLASOR_YOLU}"
            rmdir_rc=$?
            if [ ${rmdir_rc} -eq 0 ]; then
                log "MODULE_ACTION" "  RMDIR_SUCCESS: '${TAM_IC_KLASOR_YOLU}' başarıyla silindi."
            else
                log "MODULE_ACTION" "  RMDIR_ERROR: '${TAM_IC_KLASOR_YOLU}' 'rmdir' ile silinemedi (exit code ${rmdir_rc})."
                log "MODULE_ACTION" "    Klasör boş olmayabilir (taşınamayan dosya/klasörler?) veya bir izin sorunu olabilir."
                log "MODULE_ACTION" "    Lütfen '${MODUL_VEYA_SURUM_KLASORU}' içeriğini ve '${TAM_IC_KLASOR_YOLU}' (eğer hala varsa) durumunu manuel kontrol edin."
            fi
        else
            log "MODULE_ACTION" "  Skipping rmdir for '${TAM_IC_KLASOR_YOLU}' due to previous errors or conditions."
        fi
    else
        log "MODULE_LOOP" "  ATLANDI: '${MODUL_VEYA_SURUM_KLASORU}' içinde '${TASINACAK_IC_KLASOR_ADI}' adında bir alt klasör bulunamadı."
    fi
done

log "INFO" "-----------------------------------------------------"
if [ ${processed_module_count} -eq 0 ]; then
    log "INFO" "Hiçbir modül/sürüm klasörü bulunamadı veya işlenmedi: '${MULTIVERSION_ANA_DIZINI}'"
fi
log "INFO" "Tüm işlemler tamamlandı."
log "CRITICAL" "❗❗ UNUTMAYIN: Java projenizdeki paket ve import bildirimlerini IDE'nizde (örn: IntelliJ IDEA Refactor) veya manuel olarak kontrol edip düzeltmeniz GEREKECEKTİR!"
log "CRITICAL" "   Kodunuz şu anda büyük ihtimalle DERLENMEYECEKTİR!"
echo "--------------------------------------------------------------------"

# --- PENCERENİN KAPANMASINI ÖNLEMEK İÇİN EKLEME ---
read -p "Tüm logları yukarıda görebilirsiniz. Çıkmak için [Enter] tuşuna basın..."
# --- EKLEME SONU ---